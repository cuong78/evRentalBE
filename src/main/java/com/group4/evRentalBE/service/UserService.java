package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile();
    UserProfileResponse getUserByPhone(String phone);
}
