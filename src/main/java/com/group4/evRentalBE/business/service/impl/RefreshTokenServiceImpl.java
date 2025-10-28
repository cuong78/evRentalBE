package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.infrastructure.exception.exceptions.TokenRefreshException;
import com.group4.evRentalBE.business.dto.response.TokenRefreshResponse;
import com.group4.evRentalBE.domain.entity.RefreshToken;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.domain.repository.RefreshTokenRepository;
import com.group4.evRentalBE.business.service.RefreshTokenService;
import com.group4.evRentalBE.business.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private TokenService tokenService;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        // Delete any existing refresh token for this account
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    token.getToken(), "Refresh token was expired. Please make a new login request");
        }
        return token;
    }

    @Override
    public TokenRefreshResponse refreshToken(String requestRefreshToken) {
        return findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = tokenService.generateToken(user);
                    return new TokenRefreshResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(
                        requestRefreshToken, 
                        "Refresh token is not in database!"));
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}
