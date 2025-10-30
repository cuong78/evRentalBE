package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.AdminCreateUserRequest;
import com.group4.evRentalBE.business.dto.request.AdminUpdateUserRequest;
import com.group4.evRentalBE.business.dto.response.AdminUserResponse;

import java.util.List;

public interface AdminUserService   {
    // CUSTOMER
    AdminUserResponse createCustomer(AdminCreateUserRequest req);
    List<AdminUserResponse> listCustomers();
    AdminUserResponse getCustomer(Long userId);
    AdminUserResponse updateCustomer(Long userId, AdminUpdateUserRequest req);
    void deleteCustomer(Long userId);

    // STAFF
    AdminUserResponse createStaff(AdminCreateUserRequest req);
    List<AdminUserResponse> listStaffs();
    AdminUserResponse getStaff(Long userId);
    AdminUserResponse updateStaff(Long userId, AdminUpdateUserRequest req);
    void deleteStaff(Long userId);
}
