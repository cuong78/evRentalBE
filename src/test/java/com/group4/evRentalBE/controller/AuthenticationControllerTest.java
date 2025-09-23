package com.group4.evRentalBE.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.exception.exceptions.BadRequestException;
import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ForbiddenException;
import com.group4.evRentalBE.mapper.UserMapper;
import com.group4.evRentalBE.model.dto.request.*;
import com.group4.evRentalBE.model.dto.response.CustomerResponse;
import com.group4.evRentalBE.model.dto.response.TokenRefreshResponse;
import com.group4.evRentalBE.model.dto.response.UserResponse;
import com.group4.evRentalBE.model.entity.RefreshToken;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.repository.RefreshTokenRepository;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.service.AuthenticationService;
import com.group4.evRentalBE.service.EmailService;
import com.group4.evRentalBE.service.RefreshTokenService;
import com.group4.evRentalBE.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
        SecurityContextHolder.setContext(securityContext);
    }


}
