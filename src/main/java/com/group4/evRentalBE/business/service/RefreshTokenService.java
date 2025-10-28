package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.response.TokenRefreshResponse;
import com.group4.evRentalBE.domain.entity.RefreshToken;
import com.group4.evRentalBE.domain.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);
    
    // New method for controller
    TokenRefreshResponse refreshToken(String refreshToken);
    
    void deleteByUser(User user);
}