package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.model.dto.request.ContractRequest;
import com.group4.evRentalBE.model.dto.response.ContractResponse;
import com.group4.evRentalBE.service.ContractService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}