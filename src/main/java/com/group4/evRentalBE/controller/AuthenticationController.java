package com.group4.evRentalBE.controller;


import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.exception.exceptions.*;
import com.group4.evRentalBE.mapper.UserMapper;
import com.group4.evRentalBE.model.dto.request.*;
import com.group4.evRentalBE.model.dto.response.TokenRefreshResponse;
import com.group4.evRentalBE.model.dto.response.UserResponse;
import com.group4.evRentalBE.model.entity.RefreshToken;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.repository.RefreshTokenRepository;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.repository.VerificationTokenRepository;
import com.group4.evRentalBE.service.AuthenticationService;
import com.group4.evRentalBE.service.EmailService;
import com.group4.evRentalBE.service.RefreshTokenService;
import com.group4.evRentalBE.service.TokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.lang.IllegalArgumentException;
import java.util.UUID;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class AuthenticationController {

    // Constants to avoid duplicate literals
    private static final String ACCOUNT_LOCKED_MESSAGE = "Account has been locked!";
    private static final String LOGIN_SUCCESSFUL = "Login successful";

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    @Value("${frontend.url.base}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = authenticationService.register(request);
            return ResponseEntity.ok()
                    .body(new ResponseObject(
                            HttpStatus.OK.value(),
                            "Registration successful, please check email for authentication",
                            userMapper.toUserResponse(user)));
        } catch (ConflictException e) {
            throw e;
        } catch (RuntimeException e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userResponse = authenticationService.login(loginRequest);
            return ResponseEntity.ok()
                    .body(new ResponseObject(HttpStatus.OK.value(), LOGIN_SUCCESSFUL, userResponse));
        } catch (RuntimeException e) {
            // Fixed: Position literals first in String comparisons
            if (ACCOUNT_LOCKED_MESSAGE.equals(e.getMessage())) {
                // Fixed: Preserve stack trace
                throw new ForbiddenException(e.getMessage(), e);
            }
            // Fixed: Preserve stack trace
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseObject> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();
            return refreshTokenService
                    .findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String token = tokenService.generateToken(user);
                        return ResponseEntity.ok()
                                .body(new ResponseObject(
                                        HttpStatus.OK.value(),
                                        "Token refreshed successfully",
                                        new TokenRefreshResponse(token, requestRefreshToken)));
                    })
                    .orElseThrow(
                            () -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
        } catch (TokenRefreshException e) {
            // Fixed: Preserve stack trace
            throw new ForbiddenException(e.getMessage(), e);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseObject> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            User user = userRepository
                    .findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại"));

            // Tạo token reset password
            String token = UUID.randomUUID().toString();

            // Xóa tất cả token cũ của user
            authenticationService.deleteAllResetTokensByUser(user);

            // Tạo token mới
            authenticationService.createPasswordResetTokenForAccount(user, token);

            // Tạo link reset password
            String resetPasswordLink = frontendUrl + "reset-password?token=" + token;

            String emailSubject = "Yêu cầu đặt lại mật khẩu";
            String emailText = "Vui lòng nhấp vào liên kết sau để đặt lại mật khẩu của bạn:\n\n"
                    + resetPasswordLink + "\n\n"
                    + "Liên kết này sẽ hết hạn sau 1 giờ.\n"
                    + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.";

            emailService.sendEmail(request.getEmail(), emailSubject, emailText);

            return ResponseEntity.ok()
                    .body(new ResponseObject(
                            HttpStatus.OK.value(), "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.", null));
        } catch (UsernameNotFoundException e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseObject> resetPasswordWithToken(@RequestBody ResetPasswordWithTokenRequest request) {
        try {
            authenticationService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok()
                    .body(new ResponseObject(HttpStatus.OK.value(), "Đặt lại mật khẩu thành công", null));
        } catch (Exception e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "api")
    @Transactional
    public ResponseEntity<ResponseObject> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            user.incrementTokenVersion();
            userRepository.save(user);

            refreshTokenRepository.deleteByUser(user);

            return ResponseEntity.ok().body(new ResponseObject(HttpStatus.OK.value(), "Logout successful", null));
        } catch (Exception e) {
            // Fixed: Preserve stack trace
            throw new InternalServerErrorException("Logout failed: " + e.getMessage(), e);
        }
    }



    @PostMapping("/verify")
    public ResponseEntity<ResponseObject> verifyAccount(@RequestParam String token) {
        try {
            authenticationService.verifyAccount(token);
            return ResponseEntity.ok()
                    .body(new ResponseObject(HttpStatus.OK.value(), "Xác thực tài khoản thành công", null));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }


    @PostMapping("/change-password")
    @SecurityRequirement(name = "api")
    public ResponseEntity<ResponseObject> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authenticationService.changeUserPassword(request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok()
                    .body(new ResponseObject(HttpStatus.OK.value(), "Password changed successfully", null));
        } catch (UsernameNotFoundException e) {
            // Fixed: Preserve stack trace
            throw new NotFoundException("User not found", e);
        } catch (BadRequestException e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException(e.getMessage(), e);
        } catch (Exception e) {
            // Fixed: Preserve stack trace
            throw new InternalServerErrorException("Failed to change password: " + e.getMessage(), e);
        }
    }
}
