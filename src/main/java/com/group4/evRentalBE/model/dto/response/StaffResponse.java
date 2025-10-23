package com.group4.evRentalBE.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String name;
    private String role;
}
