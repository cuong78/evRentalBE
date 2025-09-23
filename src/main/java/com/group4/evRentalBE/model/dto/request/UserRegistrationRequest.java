package com.group4.evRentalBE.model.dto.request;


import java.time.LocalDate;

import jakarta.validation.constraints.*;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    @NotBlank(message = "Account cannot be blank")
    @Size(min = 6, max = 28, message = "Username must be between 6-28 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm Password cannot be blank")
    private String confirmPassword;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;


    @NotBlank(message = "Phone Number cannot be blank")
    @Pattern(regexp = "\\d{10}", message = "Invalid phone number")
    private String phone;

}
