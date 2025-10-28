package com.group4.evRentalBE.business.service.impl;



import com.group4.evRentalBE.infrastructure.constant.PredefinedRole;
import com.group4.evRentalBE.domain.entity.*;
import com.group4.evRentalBE.domain.repository.*;
import com.group4.evRentalBE.infrastructure.exception.exceptions.BadRequestException;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ConflictException;
import com.group4.evRentalBE.business.mapper.UserMapper;
import com.group4.evRentalBE.business.dto.request.LoginRequest;
import com.group4.evRentalBE.business.dto.request.UserRegistrationRequest;
import com.group4.evRentalBE.business.dto.response.CustomerResponse;
import com.group4.evRentalBE.business.dto.response.UserResponse;
import com.group4.evRentalBE.business.service.AuthenticationService;
import com.group4.evRentalBE.business.service.EmailService;
import com.group4.evRentalBE.business.service.RefreshTokenService;
import com.group4.evRentalBE.business.service.TokenService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    // Constants to avoid duplicate literals
    private static final String MEMBER_ROLE_NOT_FOUND = "MEMBER role not found";

    @Value("${frontend.url.email.verification}")
    private String emailVerificationUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public User register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        // Fixed: Use interface type instead of implementation
        Set<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.CUSTOMER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Tạo ví cho user mới với số dư 0
        createWalletForUser(savedUser);

        // Tạo verification token
        String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser, token);

        // Gửi email xác thực
        sendVerificationEmail(savedUser, token);

        return savedUser;
    }

    private void createWalletForUser(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(0L) // Số dư ban đầu là 0
                .build();
        walletRepository.save(wallet);
        log.info("Created wallet for user: {} with initial balance: 0", user.getUsername());
    }

    private void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    private void sendVerificationEmail(User user, String token) {
        String subject = "Xác thực tài khoản";
        String verificationUrl = emailVerificationUrl + "?token=" + token;
        String text = "Chào " + user.getUsername() + ",\n\n"
                + "Vui lòng nhấp vào liên kết sau để xác thực tài khoản của bạn:\n"
                + verificationUrl + "\n\n"
                + "Liên kết có hiệu lực trong 24 giờ.";

        emailService.sendEmail(user.getEmail(), subject, text);
    }

    @Override
    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new BadRequestException("Token không hợp lệ");
        }

        if (verificationToken.isExpired()) {
            throw new BadRequestException("Token đã hết hạn");
        }

        User user = verificationToken.getUser();
        user.setVerify(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    @Override
    public UserResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            User user = userRepository
                    .findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isVerify()) {
                throw new DisabledException("Account not verified. Please check your email.");
            }

        } catch (BadCredentialsException e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException("Username/ password is invalid. Please try again!", e);
        } catch (LockedException e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException("Account has been locked!", e);
        } catch (Exception e) {
            // Fixed: Preserve stack trace
            throw new BadRequestException("Login failed: " + e.getMessage(), e);
        }

        User user = userRepository
                .findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication"));

        // Tạo authentication với authorities từ permissions
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        String token = tokenService.generateToken(user);

        return UserMapper.toResponse(user, token, refreshToken.getToken());
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 3600);
        return new Date(cal.getTime().getTime());
    }

    @Override
    public User validatePasswordResetToken(String token) {
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token);
        if (passToken.getExpiryDate().before(new Date())) {
            throw new IllegalArgumentException("Token expired");
        }
        return passToken.getUser();
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        passwordResetTokenRepository.delete(resetToken);
    }




    @Override
    public void createPasswordResetTokenForAccount(User user, String token) {
        // Xóa tất cả token cũ trước khi tạo mới (đảm bảo chỉ token mới nhất có hiệu lực)
        deleteAllResetTokensByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(calculateExpiryDate());
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public void deleteAllResetTokensByUser(User user) {
        passwordResetTokenRepository.deleteByUser(user);
    }

    // Thêm phương thức mới để xử lý reset password qua token
    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        User user = validatePasswordResetToken(token);
        changePassword(user, newPassword);
        deleteResetToken(token);
    }

    @Override
    public void changeUserPassword(String oldPassword, String newPassword) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verify old password matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Fixed: Use efficient blank string check
        if (isBlankString(newPassword)) {
            throw new BadRequestException("New password cannot be empty");
        }

        if (newPassword.equals(oldPassword)) {
            throw new BadRequestException("New password must be different from old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại"));
    }

    @Override
    @Transactional
    public void logout(User user) {
        user.incrementTokenVersion();
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public CustomerResponse mapUserToCustomerResponse(User user) {
        return userMapper.toUserResponse(user);
    }

    private boolean isBlankString(String str) {
        return str == null || str.isBlank();
    }
}