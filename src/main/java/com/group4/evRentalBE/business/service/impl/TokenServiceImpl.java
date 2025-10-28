package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.infrastructure.security.JwtTokenProvider;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.business.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation của TokenService
 * Thuộc Business Logic Layer
 * Sử dụng JwtTokenProvider (Infrastructure) để xử lý technical details
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateToken(User user) {
        log.info("Generating token for user: {}", user.getUsername());
        return jwtTokenProvider.generateToken(user);
    }

    @Override
    public User validateAndGetUser(String token) {
        // Parse token để lấy claims
        Claims claims = jwtTokenProvider.getClaims(token);

        String username = claims.getSubject();

        // Lấy user từ database
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Kiểm tra token version
        int tokenVersion = jwtTokenProvider.getTokenVersion(claims);
        if (tokenVersion != user.getTokenVersion()) {
            log.warn("Token version mismatch for user: {}. Expected: {}, Got: {}",
                    username, user.getTokenVersion(), tokenVersion);
            throw new ExpiredJwtException(null, claims, "Token has been invalidated");
        }

        log.debug("Token validated successfully for user: {}", username);
        return user;
    }

    @Override
    @Transactional
    public void invalidateAllTokens(User user) {
        log.info("Invalidating all tokens for user: {}", user.getUsername());
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}