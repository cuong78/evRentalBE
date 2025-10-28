package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.response.UserProfileResponse;
import com.group4.evRentalBE.business.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "User", description = "User profile management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", 
               description = "Returns the profile information of the currently authenticated user including wallet balance, booking statistics, and documents info")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        UserProfileResponse profile = userService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/by-phone")
    @Operation(summary = "Get user profile by phone number", 
               description = "Returns the profile information of a user by their phone number including wallet balance, booking statistics, and documents info")
    public ResponseEntity<UserProfileResponse> getUserByPhone(
            @io.swagger.v3.oas.annotations.Parameter(description = "Phone number of the user", required = true)
            @org.springframework.web.bind.annotation.RequestParam String phone) {
        UserProfileResponse profile = userService.getUserByPhone(phone);
        return ResponseEntity.ok(profile);
    }
}
