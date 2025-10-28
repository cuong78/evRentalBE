package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.domain.entity.User;

/**
 * Service interface cho token operations
 * Thuộc Business Logic Layer
 */
public interface TokenService {

    /**
     * Tạo JWT token cho user
     */
    String generateToken(User user);

    /**
     * Validate token và lấy thông tin user
     * @throws io.jsonwebtoken.ExpiredJwtException nếu token hết hạn
     * @throws io.jsonwebtoken.JwtException nếu token không hợp lệ
     */
    User validateAndGetUser(String token);

    /**
     * Invalidate tất cả token của user (bằng cách tăng tokenVersion)
     */
    void invalidateAllTokens(User user);
}