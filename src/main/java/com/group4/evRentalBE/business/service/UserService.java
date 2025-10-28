package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile();
    UserProfileResponse getUserByPhone(String phone);
}
