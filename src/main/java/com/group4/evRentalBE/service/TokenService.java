package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.entity.User;

public interface TokenService {
    String generateToken(User user);

    User getAccountByToken(String token);

}
