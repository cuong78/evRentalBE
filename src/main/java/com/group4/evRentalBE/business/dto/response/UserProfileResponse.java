package com.group4.evRentalBE.business.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private boolean isVerify;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Roles
    private Set<String> roles;
    
    // Permissions
    private Set<String> permissions;
    
    // Wallet balance (if exists)
    private Long walletBalance;
    
    // Documents info
    private Integer totalDocuments;
    private Integer validDocuments;
    
    // Booking statistics
    private Long totalBookings;
    private Long activeBookings;
    private Long completedBookings;
}
