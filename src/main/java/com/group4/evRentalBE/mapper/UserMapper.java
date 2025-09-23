package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.response.CustomerResponse;
import com.group4.evRentalBE.model.dto.response.UserResponse;
import com.group4.evRentalBE.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    // Convert User -> UserResponse (cho login)
    public static UserResponse toResponse(User user, String token, String refreshToken) {
        return UserResponse.builder().token(token).refreshToken(refreshToken).build();
    }
    public CustomerResponse toUserResponse(User user) {
        return CustomerResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))
                .build();
    }
}
