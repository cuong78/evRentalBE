package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.model.dto.request.VehicleTypeRequest;
import com.group4.evRentalBE.model.dto.response.VehicleTypeResponse;
import com.group4.evRentalBE.service.VehicleTypeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-types")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping
    public ResponseEntity<ResponseObject> createVehicleType(
            @Valid @RequestBody VehicleTypeRequest vehicleTypeRequest) {
        try {
            VehicleTypeResponse response = vehicleTypeService.createVehicleType(vehicleTypeRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Vehicle type created successfully")
                            .data(response)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getAllVehicleTypes() {
        try {
            List<VehicleTypeResponse> vehicleTypes = vehicleTypeService.getAllVehicleTypes();
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle types retrieved successfully")
                            .data(vehicleTypes)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve vehicle types: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getVehicleTypeById(@PathVariable Long id) {
        try {
            VehicleTypeResponse vehicleType = vehicleTypeService.getVehicleTypeById(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle type retrieved successfully")
                            .data(vehicleType)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateVehicleType(
            @PathVariable Long id,
            @Valid @RequestBody VehicleTypeRequest vehicleTypeRequest) {
        try {
            VehicleTypeResponse updatedVehicleType = vehicleTypeService.updateVehicleType(id, vehicleTypeRequest);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle type updated successfully")
                            .data(updatedVehicleType)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteVehicleType(@PathVariable Long id) {
        try {
            vehicleTypeService.deleteVehicleType(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle type deleted successfully")
                            .data(null)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }
}