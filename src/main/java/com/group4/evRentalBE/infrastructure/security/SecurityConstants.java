package com.group4.evRentalBE.infrastructure.security;

import java.util.List;

public class SecurityConstants {

    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register",
            "/api/refresh-token",
            "/api/google-login",
            "/api/facebook-login",
            "/api/reset-password",
            "/api/forgot-password",
            "/api/verify",
            "/api/vehicles/search",
            "/api/rental-stations",
            "/api/rental-stations/*",
            "/api/rental-stations/city/*",
            "/api/payments/vnpay-return",
            "/api/wallet/topups/vnpay-return"
    );

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}