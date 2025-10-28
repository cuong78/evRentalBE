package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.BusinessRuleException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.model.dto.response.BookingDetailResponse;
import com.group4.evRentalBE.model.dto.response.CancelBookingResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.*;
import com.group4.evRentalBE.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RentalStationRepository rentalStationRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleRepository vehicleRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // Get authenticated user from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Verify user exists in database (optional safety check)
        User authenticatedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has CUSTOMER role
        if (!authenticatedUser.hasRole("CUSTOMER")) {
            throw new ResourceNotFoundException("User does not have customer role");
        }

        // BUSINESS RULE: Check if user has any pending bookings
        validateUserCanCreateBooking(authenticatedUser.getUserId());

        RentalStation station = rentalStationRepository.findById(bookingRequest.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental station not found"));

        VehicleType vehicleType = vehicleTypeRepository.findById(bookingRequest.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle type not found"));

        // ✅ BUSINESS RULE: Check vehicle availability for the requested period
        validateVehicleAvailability(
                bookingRequest.getStationId(),
                bookingRequest.getTypeId(),
                bookingRequest.getStartDate(),
                bookingRequest.getEndDate()
        );

        // Create booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setUser(authenticatedUser);
        booking.setStation(station);
        booking.setType(vehicleType);
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setPaymentMethod(Payment.PaymentMethod.VNPAY);

        // PrePersist will set createdAt, updatedAt, paymentExpiryTime and calculate totalPayment
        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    private void validateUserCanCreateBooking(Long userId) {
        long pendingBookingsCount = bookingRepository.countPendingBookingsByUserId(userId);

        if (pendingBookingsCount >= 1) {
            throw new BusinessRuleException(
                    "Cannot create new booking. You already have a pending booking. " +
                            "Please complete the payment for your current pending booking or wait for it to expire."
            );
        }
    }

    /**
     * ✅ BUSINESS RULE: Validate vehicle availability for the requested period
     * 
     * Logic:
     * 1. Lấy tất cả xe AVAILABLE của loại và station đó
     * 2. Đếm số booking đã CONFIRMED/ACTIVE trong khoảng thời gian overlap (query tối ưu)
     * 3. Tính số xe khả dụng = tổng xe - số xe đã reserved
     * 4. Nếu không còn xe khả dụng -> throw exception
     * 
     * Mục đích: Ngăn chặn overbooking - đảm bảo số booking không vượt quá số xe thực tế
     */
    private void validateVehicleAvailability(Long stationId, Long typeId, 
                                            java.time.LocalDate startDate, 
                                            java.time.LocalDate endDate) {
        
        // 1. Đếm tổng số xe AVAILABLE của loại này tại station
        long totalAvailableVehicles = vehicleRepository.findByStationIdAndTypeId(stationId, typeId)
                .stream()
                .filter(vehicle -> vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
                .count();

        if (totalAvailableVehicles == 0) {
            throw new BusinessRuleException(
                    "No vehicles available for this type at this station. " +
                    "Please choose a different vehicle type or station."
            );
        }

        // 2. Đếm số booking đã CONFIRMED hoặc ACTIVE trong khoảng thời gian overlap
        // Sử dụng query tối ưu thay vì load tất cả booking
        long reservedVehiclesCount = bookingRepository.countReservedVehicles(
                stationId, 
                typeId, 
                startDate, 
                endDate
        );

        // 3. Kiểm tra còn xe không
        long availableForBooking = totalAvailableVehicles - reservedVehiclesCount;

        if (availableForBooking <= 0) {
            throw new BusinessRuleException(
                    String.format(
                            "No vehicles available for the selected period. " +
                            "Total vehicles: %d, Already reserved: %d. " +
                            "Please choose a different date range, vehicle type, or station.",
                            totalAvailableVehicles,
                            reservedVehiclesCount
                    )
            );
        }
    }

    @Override
    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetailById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        return mapToBookingDetailResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByCustomer(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return bookingRepository.findByUserUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cancelExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndPaymentExpiryTimeBefore(
                        Booking.BookingStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    @Override
    public List<BookingResponse> getConfirmedBookingsByPhone(String phone) {
        // Tìm user bằng số điện thoại
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));

        // Lấy danh sách booking confirmed và sắp xếp theo thời gian tạo mới nhất
        List<Booking> confirmedBookings = bookingRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(
                user.getUserId(),
                Booking.BookingStatus.CONFIRMED
        );

        return confirmedBookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getActiveBookingsByPhone(String phone) {
        // Tìm user bằng số điện thoại
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));

        // Lấy danh sách booking confirmed và sắp xếp theo thời gian tạo mới nhất
        List<Booking> confirmedBookings = bookingRepository.findByUserUserIdAndStatusOrderByCreatedAtDesc(
                user.getUserId(),
                Booking.BookingStatus.ACTIVE
        );

        return confirmedBookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByStatus(String status) {
        // Validate và convert status string to enum
        Booking.BookingStatus bookingStatus;
        try {
            bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                "Invalid booking status. Valid values are: PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED"
            );
        }

        // Lấy danh sách booking theo status và sắp xếp theo thời gian tạo mới nhất
        List<Booking> bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(bookingStatus);

        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                // User information
                .userId(booking.getUser().getUserId())
                .userPhone(booking.getUser().getPhone())
                .username(booking.getUser().getUsername())
                // Station information
                .stationId(booking.getStation().getId())
                .stationName(booking.getStation().getCity())
                .stationAddress(booking.getStation().getAddress())
                // Vehicle Type information
                .typeId(booking.getType().getId())
                .typeName(booking.getType().getName())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalPayment(booking.getTotalPayment())
                .status(booking.getStatus())
                .paymentExpiryTime(booking.getPaymentExpiryTime())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        // Add vehicle information if booking is ACTIVE or COMPLETED
        if ((booking.getStatus() == Booking.BookingStatus.ACTIVE || 
             booking.getStatus() == Booking.BookingStatus.COMPLETED) && 
            booking.getContract() != null && 
            booking.getContract().getVehicle() != null) {
            
            Vehicle vehicle = booking.getContract().getVehicle();
            builder.vehicleId(vehicle.getId())
                   .vehicleName(vehicle.getType().getName() + " #" + vehicle.getId());
        }

        return builder.build();
    }

    private BookingDetailResponse mapToBookingDetailResponse(Booking booking) {
        BookingDetailResponse.BookingDetailResponseBuilder builder = BookingDetailResponse.builder()
                .id(booking.getId())
                // User information
                .userId(booking.getUser().getUserId())
                .userPhone(booking.getUser().getPhone())
                .username(booking.getUser().getUsername())
                // Station information
                .stationId(booking.getStation().getId())
                .stationName(booking.getStation().getCity())
                .stationAddress(booking.getStation().getAddress())
                // Vehicle Type information
                .typeId(booking.getType().getId())
                .typeName(booking.getType().getName())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalPayment(booking.getTotalPayment())
                .status(booking.getStatus())
                .paymentMethod(booking.getPaymentMethod())
                .paymentExpiryTime(booking.getPaymentExpiryTime())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        // Add Payment info for CONFIRMED, ACTIVE, COMPLETED
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED ||
            booking.getStatus() == Booking.BookingStatus.ACTIVE ||
            booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            
            List<BookingDetailResponse.PaymentInfo> paymentInfos = booking.getPayments().stream()
                    .map(payment -> BookingDetailResponse.PaymentInfo.builder()
                            .id(payment.getId())
                            .type(payment.getType())
                            .method(payment.getMethod())
                            .status(payment.getStatus())
                            .amount(payment.getAmount())
                            .transactionId(payment.getTransactionId())
                            .description(payment.getDescription())
                            .paymentDate(payment.getPaymentDate())
                            .createdAt(payment.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
            
            builder.payments(paymentInfos);
        }

        // Add Contract info for ACTIVE, COMPLETED
        if ((booking.getStatus() == Booking.BookingStatus.ACTIVE ||
             booking.getStatus() == Booking.BookingStatus.COMPLETED) &&
            booking.getContract() != null) {
            
            Contract contract = booking.getContract();
            BookingDetailResponse.ContractInfo contractInfo = BookingDetailResponse.ContractInfo.builder()
                    .id(contract.getId())
                    .vehicleId(contract.getVehicle().getId())
                    .vehicleName(contract.getVehicle().getType().getName() + " #" + contract.getVehicle().getId())
                    .vehicleStatus(contract.getVehicle().getStatus().toString())
                    .documentId(contract.getDocument().getId())
                    .documentType(contract.getDocument().getDocumentType().toString())
                    .documentNumber(contract.getDocument().getDocumentNumber())
                    .conditionNotes(contract.getConditionNotes())
                    .createdAt(contract.getCreatedAt())
                    .build();
            
            builder.contract(contractInfo);
        }

        // Add ReturnTransaction info for COMPLETED only
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED &&
            booking.getReturnTransaction() != null) {
            
            ReturnTransaction returnTx = booking.getReturnTransaction();
            BookingDetailResponse.ReturnTransactionInfo returnInfo = BookingDetailResponse.ReturnTransactionInfo.builder()
                    .id(returnTx.getId())
                    .returnDate(returnTx.getReturnDate())
                    .additionalFees(returnTx.getAdditionalFees())
                    .refundAmount(returnTx.getRefundAmount())
                    .conditionNotes(returnTx.getConditionNotes())
                    .photos(returnTx.getPhotos())
                    .isLateReturn(returnTx.isLateReturn())
                    .overdueDays(returnTx.getOverdueDays())
                    .createdAt(returnTx.getCreatedAt())
                    .build();
            
            builder.returnTransaction(returnInfo);
        }

        return builder.build();
    }

    @Override
    @Transactional
    public CancelBookingResponse cancelBooking(String bookingId) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        // Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        // Verify booking belongs to current user
        if (!booking.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new BusinessRuleException("You can only cancel your own bookings");
        }
        
        // RULE 1: Check if booking can be cancelled (only PENDING or CONFIRMED)
        if (booking.getStatus() != Booking.BookingStatus.PENDING && 
            booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BusinessRuleException(
                String.format("Cannot cancel booking with status %s. Only PENDING or CONFIRMED bookings can be cancelled.", 
                    booking.getStatus())
            );
        }
        
        Double refundAmount = 0.0;
        Double refundPercentage = 0.0;
        String refundReason = "";
        
        // RULE 2: Calculate refund based on status and timing
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            // PENDING: No payment made yet, no refund needed
            refundAmount = 0.0;
            refundPercentage = 0.0;
            refundReason = "Booking cancelled before payment";
            
        } else if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            // CONFIRMED: Payment made, calculate refund based on time until pickup
            
            // Get total paid amount from successful payments
            Double totalPaid = booking.getPayments().stream()
                    .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS)
                    .filter(p -> p.getType() == Payment.PaymentType.DEPOSIT)
                    .mapToDouble(Payment::getAmount)
                    .sum();
            
            if (totalPaid == 0) {
                throw new BusinessRuleException("No payment found for this booking");
            }
            
            // Calculate hours until pickup (startDate at 00:00:00)
            LocalDateTime pickupDateTime = booking.getStartDate().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilPickup = ChronoUnit.HOURS.between(now, pickupDateTime);
            
            if (hoursUntilPickup >= 24) {
                // Cancel more than 24h before pickup: 100% refund
                refundAmount = totalPaid;
                refundPercentage = 100.0;
                refundReason = String.format("Cancelled %d hours before pickup (>24h) - Full refund", hoursUntilPickup);
                
            } else if (hoursUntilPickup >= 0) {
                // Cancel within 24h before pickup: 50% refund
                refundAmount = totalPaid * 0.5;
                refundPercentage = 50.0;
                refundReason = String.format("Cancelled %d hours before pickup (<24h) - 50%% refund", hoursUntilPickup);
                
            } else {
                // Cancel after pickup time: No refund
                refundAmount = 0.0;
                refundPercentage = 0.0;
                refundReason = "Cancelled after scheduled pickup time - No refund";
            }
            
            // Process refund to wallet
            if (refundAmount > 0) {
                Wallet wallet = walletRepository.findByUserUserId(currentUser.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
                
                wallet.credit(refundAmount.longValue());
                walletRepository.save(wallet);
                
                // Create refund payment record
                Payment refundPayment = Payment.builder()
                        .booking(booking)
                        .type(Payment.PaymentType.REFUND)
                        .method(Payment.PaymentMethod.WALLET)
                        .status(Payment.PaymentStatus.SUCCESS)
                        .amount(refundAmount)
                        .description(refundReason)
                        .transactionId("REFUND_" + UUID.randomUUID().toString())
                        .paymentDate(LocalDateTime.now())
                        .build();
                
                paymentRepository.save(refundPayment);
                
                log.info("Refund processed: Booking={}, Amount={}, Percentage={}%", 
                    bookingId, refundAmount, refundPercentage);
            }
        }
        
        // Update booking status to CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Get updated wallet balance
        Long walletBalance = walletRepository.findByUserUserId(currentUser.getUserId())
                .map(Wallet::getBalance)
                .orElse(0L);
        
        log.info("Booking cancelled: BookingId={}, RefundAmount={}, RefundPercentage={}%", 
            bookingId, refundAmount, refundPercentage);
        
        return CancelBookingResponse.builder()
                .bookingId(bookingId)
                .message("Booking cancelled successfully")
                .refundAmount(refundAmount)
                .refundPercentage(refundPercentage)
                .refundReason(refundReason)
                .cancelledAt(LocalDateTime.now())
                .walletBalance(walletBalance)
                .build();
    }


}
