package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.dto.request.ContractRequest;
import com.group4.evRentalBE.model.dto.response.ContractResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.BookingRepository;
import com.group4.evRentalBE.repository.ContractRepository;
import com.group4.evRentalBE.repository.VehicleRepository;
import com.group4.evRentalBE.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest contractRequest) {
        Booking booking = bookingRepository.findById(contractRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate booking status
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can create contracts");
        }



        Vehicle vehicle = vehicleRepository.findById(contractRequest.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Validate vehicle availability
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is not available");
        }

        // Check if contract already exists
        if (contractRepository.existsByBookingId(contractRequest.getBookingId())) {
            throw new IllegalStateException("Contract already exists for this booking");
        }

        // Create contract
        Contract contract = new Contract();
        contract.setBooking(booking);
        contract.setVehicle(vehicle);
        contract.setCccd(contractRequest.getCccd());
        contract.setConditionNotes(contractRequest.getConditionNotes());
        contract.setSignaturePhoto(contractRequest.getSignaturePhoto());
        contract.setVehiclePhoto(contractRequest.getVehiclePhoto());

        Contract savedContract = contractRepository.save(contract);

        // Update booking status to ACTIVE
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        bookingRepository.save(booking);

        // Update vehicle status to RENTED
        vehicle.rentOut();
        vehicleRepository.save(vehicle);

        return mapToContractResponse(savedContract);
    }

    @Override
    public ContractResponse getContractByBookingId(String bookingId) {
        Contract contract = contractRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        return mapToContractResponse(contract);
    }

    private ContractResponse mapToContractResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .bookingId(contract.getBooking().getId())
                .vehicleId(contract.getVehicle().getId())
                .cccd(contract.getCccd())
                .conditionNotes(contract.getConditionNotes())
                .signaturePhoto(contract.getSignaturePhoto())
                .vehiclePhoto(contract.getVehiclePhoto())
                .invoiceDetails(contract.getInvoiceDetails())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}