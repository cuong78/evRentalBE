package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.business.dto.request.AdminCreateUserRequest;
import com.group4.evRentalBE.business.dto.request.AdminUpdateUserRequest;
import com.group4.evRentalBE.business.dto.response.AdminUserResponse;
import com.group4.evRentalBE.business.service.AdminUserService;
import com.group4.evRentalBE.domain.entity.RentalStation;
import com.group4.evRentalBE.domain.entity.Role;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.domain.repository.RentalStationRepository;
import com.group4.evRentalBE.domain.repository.RoleRepository;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.infrastructure.constant.PredefinedRole;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final RentalStationRepository stationRepo; // nếu có STAFF quản lý trạm
    private final PasswordEncoder passwordEncoder;

    // ---------- CUSTOMER ----------
    @Override
    @Transactional
    public AdminUserResponse createCustomer(AdminCreateUserRequest req) {
        validateUnique(req.getUsername(), req.getEmail(), req.getPhone());

        Role customer = roleRepo.findById(PredefinedRole.CUSTOMER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(new HashSet<>(Collections.singletonList(customer)))
                .build();

        User saved = userRepo.save(user);
        log.info("Created CUSTOMER userId={}", saved.getUserId());
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listCustomers() {
        return userRepo.findAllByRole(PredefinedRole.CUSTOMER_ROLE)
                .stream().map(this::map).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getCustomer(Long userId) {
        return userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.CUSTOMER_ROLE))
                .map(this::map)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    @Transactional
    public AdminUserResponse updateCustomer(Long userId, AdminUpdateUserRequest req) {
        User user = userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.CUSTOMER_ROLE))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        applyUpdates(user, req, false);
        User saved = userRepo.save(user);
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long userId) {
        User user = userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.CUSTOMER_ROLE))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        userRepo.delete(user);
        log.info("Deleted CUSTOMER userId={}", userId);
    }

    // ---------- STAFF ----------
    @Override
    @Transactional
    public AdminUserResponse createStaff(AdminCreateUserRequest req) {
        validateUnique(req.getUsername(), req.getEmail(), req.getPhone());

        Role staff = roleRepo.findById(PredefinedRole.STAFF_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role STAFF not found"));

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(new HashSet<>(Collections.singletonList(staff)))
                .build();

        if (req.getManagedStationId() != null) {
            RentalStation station = stationRepo.findById(req.getManagedStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Station not found"));
            user.setManagedStation(station);
        }

        User saved = userRepo.save(user);
        log.info("Created STAFF userId={}", saved.getUserId());
        return map(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listStaffs() {
        return userRepo.findAllByRole(PredefinedRole.STAFF_ROLE)
                .stream().map(this::map).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getStaff(Long userId) {
        return userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.STAFF_ROLE))
                .map(this::map)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @Override
    @Transactional
    public AdminUserResponse updateStaff(Long userId, AdminUpdateUserRequest req) {
        User user = userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.STAFF_ROLE))
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        applyUpdates(user, req, true);
        User saved = userRepo.save(user);
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteStaff(Long userId) {
        User user = userRepo.findById(userId)
                .filter(u -> u.hasRole(PredefinedRole.STAFF_ROLE))
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        userRepo.delete(user);
        log.info("Deleted STAFF userId={}", userId);
    }

    // ---------- helpers ----------
    private void validateUnique(String username, String email, String phone) {
        if (userRepo.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepo.existsByEmail(email)) throw new IllegalArgumentException("Email already exists");
        if (userRepo.existsByPhone(phone)) throw new IllegalArgumentException("Phone already exists");
    }

    private void applyUpdates(User user, AdminUpdateUserRequest req, boolean isStaff) {
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (userRepo.existsByUsername(req.getUsername()) && !req.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(req.getUsername());
        }
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getPassword() != null && !req.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getIsVerify() != null) user.setVerify(req.getIsVerify());
        if (isStaff && req.getManagedStationId() != null) {
            var station = stationRepo.findById(req.getManagedStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Station not found"));
            user.setManagedStation(station);
        }
    }


    private AdminUserResponse map(User u) {
        return AdminUserResponse.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .phone(u.getPhone())
                .verify(u.isVerify())
                .roles(u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .managedStationId(u.getManagedStation() != null ? u.getManagedStation().getId() : null)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
