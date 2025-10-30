package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.AdminCreateUserRequest;
import com.group4.evRentalBE.business.dto.request.AdminUpdateUserRequest;
import com.group4.evRentalBE.business.dto.response.AdminUserResponse;
import com.group4.evRentalBE.business.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "Admin - Users", description = "CRUD Staff & Customer (Admin only)")
public class AdminUserController {

    private final AdminUserService service;

    // ========== CUSTOMER ==========

    @PostMapping("/customers")
    @Operation(summary = "Create Customer", description = "Admin creates a new customer account")
    public ResponseEntity<AdminUserResponse> createCustomer(@Valid @RequestBody AdminCreateUserRequest req) {
        AdminUserResponse response = service.createCustomer(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    @Operation(summary = "List all customers", description = "Retrieve list of all customers in the system")
    public ResponseEntity<List<AdminUserResponse>> listCustomers() {
        List<AdminUserResponse> response = service.listCustomers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer information by ID")
    public ResponseEntity<AdminUserResponse> getCustomer(@PathVariable Long id) {
        AdminUserResponse response = service.getCustomer(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{id}")
    @Operation(summary = "Update customer", description = "Update customer information by ID")
    public ResponseEntity<AdminUserResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest req) {
        AdminUserResponse response = service.updateCustomer(id, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/customers/{id}")
    @Operation(summary = "Delete customer", description = "Delete customer by ID")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ========== STAFF ==========

    @PostMapping("/staffs")
    @Operation(summary = "Create Staff", description = "Admin creates a new staff account")
    public ResponseEntity<AdminUserResponse> createStaff(@Valid @RequestBody AdminCreateUserRequest req) {
        AdminUserResponse response = service.createStaff(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staffs")
    @Operation(summary = "List all staff", description = "Retrieve list of all staff in the system")
    public ResponseEntity<List<AdminUserResponse>> listStaffs() {
        List<AdminUserResponse> response = service.listStaffs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staffs/{id}")
    @Operation(summary = "Get staff by ID", description = "Retrieve staff information by ID")
    public ResponseEntity<AdminUserResponse> getStaff(@PathVariable Long id) {
        AdminUserResponse response = service.getStaff(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/staffs/{id}")
    @Operation(summary = "Update staff", description = "Update staff information by ID")
    public ResponseEntity<AdminUserResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest req) {
        AdminUserResponse response = service.updateStaff(id, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/staffs/{id}")
    @Operation(summary = "Delete staff", description = "Delete staff by ID")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        service.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }
}
