package com.group4.evRentalBE.business.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private boolean verify;
    private Set<String> roles;
    private Long managedStationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
