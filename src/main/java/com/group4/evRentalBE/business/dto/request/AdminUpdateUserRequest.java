package com.group4.evRentalBE.business.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUpdateUserRequest {
    @Email private String email;
    private String phone;
    private String password;      // optional: đổi mật khẩu
    private Boolean isVerify;
    private Long managedStationId; // chỉ áp dụng cho STAFF
}