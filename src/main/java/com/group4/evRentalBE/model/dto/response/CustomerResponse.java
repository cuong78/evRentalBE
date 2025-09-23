package com.group4.evRentalBE.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Set<String> roles;
}
