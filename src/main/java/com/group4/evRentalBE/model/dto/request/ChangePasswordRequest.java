package com.group4.evRentalBE.model.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Old password cannot be blank")
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    private String newPassword;
}
