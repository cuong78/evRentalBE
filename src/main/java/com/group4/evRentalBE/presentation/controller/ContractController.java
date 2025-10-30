package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.ContractRequest;
import com.group4.evRentalBE.business.dto.request.ContractFilterRequest;
import com.group4.evRentalBE.business.dto.response.ContractResponse;
import com.group4.evRentalBE.business.service.ContractService;
import com.group4.evRentalBE.infrastructure.constant.ResponseObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@RequestBody ContractRequest contractRequest) {
        ContractResponse response = contractService.createContract(contractRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ContractResponse> getContractByBooking(@PathVariable String bookingId) {
        ContractResponse response = contractService.getContractByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<ResponseObject> getAllContracts() {
        try {
            List<ContractResponse> contracts = contractService.getAllContracts();

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Contracts retrieved successfully")
                            .data(contracts)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve contracts: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }


    @PostMapping("/filter")
    public ResponseEntity<ResponseObject> getContractsFiltered(@RequestBody ContractFilterRequest request) {
        try {
            List<ContractResponse> contracts = contractService.getContractsFiltered(
                    request.getStationId(),
                    request.getVehicleTypeId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Contracts filtered successfully")
                            .data(contracts)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ResponseObject.builder()
                            .statusCode(500)
                            .message("Failed to filter contracts: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}





















