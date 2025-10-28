package com.group4.evRentalBE.infrastructure.config;

import com.group4.evRentalBE.infrastructure.security.JwtAuthenticationFilter;
import com.group4.evRentalBE.infrastructure.security.SecurityConstants;
import com.group4.evRentalBE.business.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * Spring Security configuration
 * Thuộc Infrastructure/Config layer
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy
    private final AuthenticationService authenticationService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép CORS pre-flight requests
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        // Cho phép các public endpoints
                        .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS.toArray(new String[0]))
                        .permitAll()
                        // Tất cả request khác cần authentication
                        .anyRequest().authenticated()
                )
                .userDetailsService(authenticationService)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}