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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;

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
    @Override
    public List<ContractResponse> getContractsFiltered(Long stationId, Long vehicleTypeId,
                                                       LocalDate startDate, LocalDate endDate) {
        Specification<Contract> spec = (root, query, cb) -> cb.conjunction();

        if (stationId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("booking").get("station").get("id"), stationId));
        }

        if (vehicleTypeId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("booking").get("type").get("id"), vehicleTypeId));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
        }

        List<Contract> contracts = contractRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        return contracts.stream()
                .map(this::toResponse)
                .toList();
    }

    private ContractResponse toResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .bookingId(contract.getBooking() != null ? contract.getBooking().getId() : null)
                .vehicleId(contract.getVehicle() != null ? contract.getVehicle().getId() : null)
                .stationId(contract.getBooking() != null ? contract.getBooking().getStation().getId() : null)
                .vehicleTypeId(contract.getBooking() != null ? contract.getBooking().getType().getId() : null)
                .documentId(contract.getDocument().getId())
                .conditionNotes(contract.getConditionNotes())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
    @Override
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
