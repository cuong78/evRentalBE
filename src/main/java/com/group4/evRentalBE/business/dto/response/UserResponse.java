package com.group4.evRentalBE.business.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    public String token;
    public String refreshToken;
}
