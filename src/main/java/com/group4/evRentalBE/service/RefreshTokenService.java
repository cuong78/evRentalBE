package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.entity.RefreshToken;
import com.group4.evRentalBE.model.entity.User;

import java.util.Optional;



public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);
}