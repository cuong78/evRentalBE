package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.model.dto.response.ReturnTransactionResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.*;
import com.group4.evRentalBE.service.ReturnTransactionService;
import com.group4.evRentalBE.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnTransactionServiceImpl implements ReturnTransactionService {

    private final ReturnTransactionRepository returnTransactionRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public ReturnTransactionResponse createReturnTransaction(ReturnTransactionRequest returnTransactionRequest) {
        Booking booking = bookingRepository.findById(returnTransactionRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate booking status
        if (booking.getStatus() != Booking.BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only active bookings can be returned");
        }

        Contract contract = booking.getContract();
        if (contract == null) {
            throw new IllegalStateException("Contract not found for this booking");
        }

        Vehicle vehicle = contract.getVehicle();

        // Upload photos to cloud
        String photosUrl = null;
        if (returnTransactionRequest.getPhotos() != null && returnTransactionRequest.getPhotos().length > 0) {
            photosUrl = fileUploadService.uploadMultipleFiles(
                    returnTransactionRequest.getPhotos(), 
                    "returns"
            );
            log.info("Return transaction photos uploaded: {}", photosUrl);
        }

        // Create return transaction
        ReturnTransaction returnTransaction = new ReturnTransaction();
        returnTransaction.setBooking(booking);
        returnTransaction.setReturnDate(LocalDateTime.now());
        returnTransaction.setConditionNotes(returnTransactionRequest.getConditionNotes());
        returnTransaction.setPhotos(photosUrl); // Store comma-separated URLs

        // Calculate additional fees and refund amount
        calculateFeesAndRefund(returnTransaction, booking, returnTransactionRequest);

        ReturnTransaction savedReturnTransaction = returnTransactionRepository.save(returnTransaction);
        log.info("Return transaction created with ID: {}", savedReturnTransaction.getId());

        // Update vehicle status to AVAILABLE
        vehicle.returnVehicle();
        if (returnTransactionRequest.getConditionNotes() != null &&
                returnTransactionRequest.getConditionNotes().toLowerCase().contains("damage")) {
            vehicle.markAsDamaged(returnTransactionRequest.getConditionNotes());
        }
        vehicleRepository.save(vehicle);

        // Update booking status to COMPLETED
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Process refund if applicable
        if (savedReturnTransaction.getRefundAmount() > 0) {
            processRefund(booking, savedReturnTransaction);
        }

        return mapToReturnTransactionResponse(savedReturnTransaction);
    }

    @Override
    public ReturnTransactionResponse getReturnTransactionByBookingId(String bookingId) {
        ReturnTransaction returnTransaction = returnTransactionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Return transaction not found"));
        return mapToReturnTransactionResponse(returnTransaction);
    }

    private void calculateFeesAndRefund(ReturnTransaction returnTransaction,
                                        Booking booking,
                                        ReturnTransactionRequest request) {
        double depositAmount = booking.getType().getDepositAmount();
        double additionalFees = 0.0;

        // Calculate late return fees
        if (returnTransaction.isLateReturn()) {
            long overdueDays = returnTransaction.getOverdueDays();
            double dailyRate = booking.getType().getRentalRate();
            additionalFees += overdueDays * dailyRate * 1.5; // 150% of daily rate for overdue
        }

        // Calculate damage fees
        if (request.getDamageFee() != null) {
            additionalFees += request.getDamageFee();
        }


        returnTransaction.setAdditionalFees(additionalFees);

        // Calculate refund amount (deposit - additional fees)
        double refundAmount = Math.max(depositAmount - additionalFees, 0);
        returnTransaction.setRefundAmount(refundAmount);
    }

    private void processRefund(Booking booking, ReturnTransaction returnTransaction) {
        String description = "Refund for booking " + booking.getId() +
                ". Additional fees: " + returnTransaction.getAdditionalFees();

        User customer = booking.getUser();
        
        // Find customer's wallet
        Wallet wallet = walletRepository.findByUserUserId(customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        // Convert refund amount to VND (long)
        long refundAmountVnd = returnTransaction.getRefundAmount().longValue();

        // Credit wallet
        wallet.credit(refundAmountVnd);
        walletRepository.save(wallet);

        // Create refund payment record
        Payment refundPayment = Payment.builder()
                .booking(booking)
                .type(Payment.PaymentType.REFUND)
                .method(Payment.PaymentMethod.WALLET) // Always refund to wallet
                .status(Payment.PaymentStatus.SUCCESS)
                .amount(returnTransaction.getRefundAmount())
                .transactionId("REFUND_" + UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .description(description)
                .build();

        paymentRepository.save(refundPayment);

        log.info("Refunded {} VND to wallet for user {} (booking: {})", 
                refundAmountVnd, customer.getUserId(), booking.getId());
    }

    private ReturnTransactionResponse mapToReturnTransactionResponse(ReturnTransaction returnTransaction) {
        return ReturnTransactionResponse.builder()
                .id(returnTransaction.getId())
                .bookingId(returnTransaction.getBooking().getId())
                .returnDate(returnTransaction.getReturnDate())
                .additionalFees(returnTransaction.getAdditionalFees())
                .refundAmount(returnTransaction.getRefundAmount())
                .conditionNotes(returnTransaction.getConditionNotes())
                .photos(returnTransaction.getPhotos())
                .createdAt(returnTransaction.getCreatedAt())
                .updatedAt(returnTransaction.getUpdatedAt())
                .build();
    }
}
