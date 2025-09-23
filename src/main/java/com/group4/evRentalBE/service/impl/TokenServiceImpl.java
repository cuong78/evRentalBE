package com.group4.evRentalBE.service.impl;


import com.group4.evRentalBE.model.entity.Permission;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private int jwtExpirationMs;

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenVersion", user.getTokenVersion());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        claims.put(
                "roles",
                user.getRoles().stream().map(role -> "ROLE_" + role.getName()).collect(Collectors.toList()));

        claims.put(
                "permissions",
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getCode) // Use CODE instead of name
                        .collect(Collectors.toSet()));

        return Jwts.builder()
                .setClaims(claims) // Đặt claims trước
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(getSigninKey(), SignatureAlgorithm.HS512) // Sử dụng SecretKey
                .compact();
    }

    @Override
    public User getAccountByToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = (String) claims.get("username");
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found with username: " + username));

        // Kiểm tra token version
        int tokenVersion = (int) claims.get("tokenVersion");
        if (tokenVersion != user.getTokenVersion()) {
            throw new ExpiredJwtException(null, claims, "Token has been invalidated");
        }

        return user;
    }

}
