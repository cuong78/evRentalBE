package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.domain.entity.*;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.business.dto.request.ContractRequest;
import com.group4.evRentalBE.business.dto.response.ContractResponse;
import com.group4.evRentalBE.domain.repository.BookingRepository;
import com.group4.evRentalBE.domain.repository.ContractRepository;
import com.group4.evRentalBE.domain.repository.DocumentRepository;
import com.group4.evRentalBE.domain.repository.VehicleRepository;
import com.group4.evRentalBE.business.service.ContractService;
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

        // Check if vehicle already has an active contract
        if (contractRepository.existsActiveContractByVehicleId(vehicle.getId())) {
            throw new IllegalStateException("Vehicle is already in an active contract");
        }

        // Validate vehicle type matches booking type
        if (!vehicle.getType().getId().equals(booking.getType().getId())) {
            throw new IllegalStateException(
                String.format("Vehicle type mismatch: Booking requires type '%s' (ID: %d), but vehicle has type '%s' (ID: %d)",
                    booking.getType().getName(),
                    booking.getType().getId(),
                    vehicle.getType().getName(),
                    vehicle.getType().getId())
            );
        }

        // Validate vehicle is at the correct station
        if (!vehicle.getStation().getId().equals(booking.getStation().getId())) {
            throw new IllegalStateException(
                String.format("Vehicle station mismatch: Booking is for station in '%s' (ID: %d), but vehicle is at station in '%s' (ID: %d)",
                    booking.getStation().getCity(),
                    booking.getStation().getId(),
                    vehicle.getStation().getCity(),
                    vehicle.getStation().getId())
            );
        }

        // Check if contract already exists
        if (contractRepository.existsByBookingId(contractRequest.getBookingId())) {
            throw new IllegalStateException("Contract already exists for this booking");
        }

        // Get customer from booking
        User customer = booking.getUser();
        
        // Find customer's valid CCCD or GPLX document
        Document document = findValidDocument(customer);
        if (document == null) {
            throw new IllegalStateException("Customer must upload CCCD or GPLX document before creating contract");
        }

        // Create contract
        Contract contract = new Contract();
        contract.setBooking(booking);
        contract.setVehicle(vehicle);
        contract.setDocument(document);
        contract.setConditionNotes(contractRequest.getConditionNotes());

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

    /**
     * Find a valid CCCD or GPLX document for the customer
     * Priority: CCCD > GPLX
     */
    private Document findValidDocument(User customer) {
        // First try to find CCCD
        Document cccdDocument = customer.getDocuments().stream()
                .filter(doc -> doc.getDocumentType() == Document.DocumentType.CCCD)
                .filter(Document::isValid)
                .findFirst()
                .orElse(null);
        
        if (cccdDocument != null) {
            return cccdDocument;
        }
        
        // If no CCCD, try to find GPLX
        Document gplxDocument = customer.getDocuments().stream()
                .filter(doc -> doc.getDocumentType() == Document.DocumentType.DRIVING_LICENSE)
                .filter(Document::isValid)
                .findFirst()
                .orElse(null);
        
        return gplxDocument;
    }

    private ContractResponse mapToContractResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .bookingId(contract.getBooking().getId())
                .vehicleId(contract.getVehicle().getId())
                .documentId(contract.getDocument().getId())
                .conditionNotes(contract.getConditionNotes())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
