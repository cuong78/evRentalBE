package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.LoginRequest;
import com.group4.evRentalBE.business.dto.request.UserRegistrationRequest;
import com.group4.evRentalBE.business.dto.response.CustomerResponse;
import com.group4.evRentalBE.business.dto.response.UserResponse;
import com.group4.evRentalBE.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthenticationService extends UserDetailsService {
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
    
    // New methods for controller
    User findUserByEmail(String email);
    
    void logout(User user);
    
    CustomerResponse mapUserToCustomerResponse(User user);
}
