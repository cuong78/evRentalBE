package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.business.dto.response.UserProfileResponse;
import com.group4.evRentalBE.domain.entity.Role;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.domain.entity.Wallet;
import com.group4.evRentalBE.domain.entity.Booking;
import com.group4.evRentalBE.domain.repository.BookingRepository;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.domain.repository.WalletRepository;
import com.group4.evRentalBE.business.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        // Get authenticated user from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        // Fetch fresh user data from database
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get wallet balance
        Long walletBalance = walletRepository.findByUserUserId(user.getUserId())
                .map(Wallet::getBalance)
                .orElse(0L);
        
        // Get booking statistics
        long totalBookings = bookingRepository.countByUserUserId(user.getUserId());
        long activeBookings = bookingRepository.countByUserUserIdAndStatus(
                user.getUserId(), 
                Booking.BookingStatus.ACTIVE
        );
        long completedBookings = bookingRepository.countByUserUserIdAndStatus(
                user.getUserId(), 
                Booking.BookingStatus.COMPLETED
        );
        
        // Get roles
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        // Get permissions
        var permissions = user.getAllPermissions();
        
        // Get documents info
        int totalDocuments = user.getDocuments().size();
        int validDocuments = (int) user.getDocuments().stream()
                .filter(doc -> doc.isValid())
                .count();
        
        log.info("User profile retrieved for userId: {}", user.getUserId());
        
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isVerify(user.isVerify())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .permissions(permissions)
                .walletBalance(walletBalance)
                .totalDocuments(totalDocuments)
                .validDocuments(validDocuments)
                .totalBookings(totalBookings)
                .activeBookings(activeBookings)
                .completedBookings(completedBookings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserByPhone(String phone) {
        log.info("Fetching user profile by phone: {}", phone);
        
        // Find user by phone number
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));
        
        // Get wallet balance
        Long walletBalance = walletRepository.findByUserUserId(user.getUserId())
                .map(Wallet::getBalance)
                .orElse(0L);
        
        // Get booking statistics
        long totalBookings = bookingRepository.countByUserUserId(user.getUserId());
        long activeBookings = bookingRepository.countByUserUserIdAndStatus(
                user.getUserId(), 
                Booking.BookingStatus.ACTIVE
        );
        long completedBookings = bookingRepository.countByUserUserIdAndStatus(
                user.getUserId(), 
                Booking.BookingStatus.COMPLETED
        );
        
        // Get roles
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        // Get permissions
        var permissions = user.getAllPermissions();
        
        // Get documents info
        int totalDocuments = user.getDocuments().size();
        int validDocuments = (int) user.getDocuments().stream()
                .filter(doc -> doc.isValid())
                .count();
        
        log.info("User profile retrieved by phone for userId: {}", user.getUserId());
        
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isVerify(user.isVerify())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .permissions(permissions)
                .walletBalance(walletBalance)
                .totalDocuments(totalDocuments)
                .validDocuments(validDocuments)
                .totalBookings(totalBookings)
                .activeBookings(activeBookings)
                .completedBookings(completedBookings)
                .build();
    }
}
