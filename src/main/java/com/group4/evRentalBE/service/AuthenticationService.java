package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.LoginRequest;
import com.group4.evRentalBE.model.dto.request.UserRegistrationRequest;
import com.group4.evRentalBE.model.dto.response.UserResponse;
import com.group4.evRentalBE.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthenticationService  extends UserDetailsService {
    User register(UserRegistrationRequest request);

    UserResponse login(LoginRequest loginRequest);

    void createPasswordResetTokenForAccount(User user, String token);

    User validatePasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    void deleteResetToken(String token);


    void verifyAccount(String token);



    void deleteAllResetTokensByUser(User user);

    void resetPasswordWithToken(String token, String newPassword);

    void changeUserPassword(String oldPassword, String newPassword);
}
