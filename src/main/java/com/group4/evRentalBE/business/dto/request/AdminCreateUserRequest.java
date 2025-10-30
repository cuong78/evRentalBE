package com.group4.evRentalBE.business.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {
    @NotBlank
    private String username;
    @Email
    @NotBlank private String email;
    @NotBlank private String phone;
    @NotBlank private String password;

    // Dùng khi tạo STAFF, có thể null nếu không gán station
    private Long managedStationId;
}
