package com.group4.evRentalBE.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.evRentalBE.business.mapper.UserMapper;
import com.group4.evRentalBE.domain.repository.RefreshTokenRepository;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.presentation.controller.AuthenticationController;
import com.group4.evRentalBE.business.service.AuthenticationService;
import com.group4.evRentalBE.business.service.EmailService;
import com.group4.evRentalBE.business.service.RefreshTokenService;
import com.group4.evRentalBE.business.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
