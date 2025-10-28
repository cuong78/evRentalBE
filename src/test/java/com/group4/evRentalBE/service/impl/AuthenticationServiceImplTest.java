package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.business.service.impl.AuthenticationServiceImpl;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.infrastructure.exception.exceptions.BadRequestException;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ConflictException;
import com.group4.evRentalBE.business.dto.request.LoginRequest;
import com.group4.evRentalBE.business.dto.request.UserRegistrationRequest;
import com.group4.evRentalBE.domain.repository.PasswordResetTokenRepository;
import com.group4.evRentalBE.domain.repository.RoleRepository;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.domain.repository.VerificationTokenRepository;
import com.group4.evRentalBE.business.service.EmailService;
import com.group4.evRentalBE.business.service.RefreshTokenService;
import com.group4.evRentalBE.business.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void register_UsernameExists() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setPhone("1234567890");

        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authenticationService.register(request));
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailExists() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("existing@example.com");
        request.setPhone("1234567890");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authenticationService.register(request));
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_PhoneExists() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setPhone("existingphone");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authenticationService.register(request));
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository).existsByEmail(anyString());
        verify(userRepository).existsByPhone(anyString());
        verify(userRepository, never()).save(any(User.class));
    }



    @Test
    void login_BadCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authenticationService.login(loginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(User.class));
    }


}
