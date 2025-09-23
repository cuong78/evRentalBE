package com.group4.evRentalBE.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordWithTokenRequest {
    private String token;
    private String newPassword;
}