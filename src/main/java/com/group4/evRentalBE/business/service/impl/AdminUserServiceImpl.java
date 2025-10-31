package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.business.dto.request.AdminCreateUserRequest;
import com.group4.evRentalBE.business.dto.request.AdminUpdateUserRequest;
import com.group4.evRentalBE.business.dto.response.AdminUserResponse;
import com.group4.evRentalBE.business.service.AdminUserService;
import com.group4.evRentalBE.domain.entity.*;
import com.group4.evRentalBE.domain.repository.*;
import com.group4.evRentalBE.infrastructure.constant.PredefinedRole;
import com.group4.evRentalBE.infrastructure.exception.exceptions.BadRequestException;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final BookingRepository bookingRepo;
    private final WalletRepository walletRepo;

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
                .isVerify(true)
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

        long totalBookings = bookingRepo.countByUserUserId(user.getUserId());
        if (totalBookings > 0) {
            throw new BadRequestException("Cannot delete user: this customer has booking(s).");
        }

        long pending   = bookingRepo.countByUserUserIdAndStatus(user.getUserId(), Booking.BookingStatus.PENDING);
        long confirmed = bookingRepo.countByUserUserIdAndStatus(user.getUserId(), Booking.BookingStatus.CONFIRMED);
        long active    = bookingRepo.countByUserUserIdAndStatus(user.getUserId(), Booking.BookingStatus.ACTIVE);
        long unfinished = pending + confirmed + active;
        if (unfinished > 0) {
            throw new BadRequestException(
                    "Customer still has " + unfinished + " unfinished booking(s). Deletion is not allowed."
            );
        }

        // (C) Ví phải = 0 (nếu có) → xóa ví trước để tránh FK
        Wallet wallet = walletRepo.findByUserUserId(user.getUserId()).orElse(null);
        if (wallet != null) {
            Long balance = wallet.getBalance(); // kiểu Long trong dự án của bạn
            if (balance != null && balance != 0L) {
                throw new BadRequestException("Wallet balance must be zero before deleting this user.");
            }
            walletRepo.delete(wallet); // hoặc walletRepo.deleteByUser_UserId(user.getUserId());
        }

        // (D) Nếu bạn có bảng refresh_token → dọn trước (tránh FK)
        // refreshTokenRepo.deleteByUserId(user.getUserId());

        // (E) Cuối cùng xoá user
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
                .isVerify(true)
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

        var station = user.getManagedStation();
        if (station != null) {
            long staffCount = userRepo.countByManagedStation_IdAndRoles_Name(
                    station.getId(), PredefinedRole.STAFF_ROLE);

            if (staffCount <= 1) {
                throw new BadRequestException(
                        "This station has only one managing staff. Deletion is not allowed."
                );
            }
        }

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
