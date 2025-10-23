package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.dto.request.VehicleRequest;
import com.group4.evRentalBE.model.dto.response.VehicleAvailabilityResponse;
import com.group4.evRentalBE.model.dto.response.VehicleResponse;
import com.group4.evRentalBE.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor

public class VehicleController {

    private final VehicleService vehicleService;

    @SecurityRequirement(name = "api")
    @PostMapping
    public ResponseEntity<ResponseObject> createVehicle(
            @Valid @RequestBody VehicleRequest vehicleRequest) {
        try {
            VehicleResponse response = vehicleService.createVehicle(vehicleRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Vehicle created successfully")
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

    @SecurityRequirement(name = "api")
    @GetMapping
    public ResponseEntity<ResponseObject> getAllVehicles() {
        try {
            List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicles retrieved successfully")
                            .data(vehicles)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve vehicles: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @SecurityRequirement(name = "api")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getVehicleById(@PathVariable Long id) {
        try {
            VehicleResponse vehicle = vehicleService.getVehicleById(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle retrieved successfully")
                            .data(vehicle)
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

    @SecurityRequirement(name = "api")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest vehicleRequest) {
        try {
            VehicleResponse updatedVehicle = vehicleService.updateVehicle(id, vehicleRequest);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle updated successfully")
                            .data(updatedVehicle)
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

    @SecurityRequirement(name = "api")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle deleted successfully")
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

    @SecurityRequirement(name = "api")
    // Additional endpoints
    @GetMapping("/station/{stationId}")
    public ResponseEntity<ResponseObject> getVehiclesByStation(@PathVariable Long stationId) {
        try {
            List<VehicleResponse> vehicles = vehicleService.getVehiclesByStation(stationId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicles retrieved successfully")
                            .data(vehicles)
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

    @SecurityRequirement(name = "api")
    @GetMapping("/type/{typeId}")
    public ResponseEntity<ResponseObject> getVehiclesByType(@PathVariable Long typeId) {
        try {
            List<VehicleResponse> vehicles = vehicleService.getVehiclesByType(typeId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicles retrieved successfully")
                            .data(vehicles)
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



    @SecurityRequirement(name = "api")
    @GetMapping("/station/{stationId}/type/{typeId}")
    public ResponseEntity<ResponseObject> getVehiclesByStationAndType(
            @PathVariable Long stationId,
            @PathVariable Long typeId) {
        try {
            List<VehicleResponse> vehicles = vehicleService.getVehiclesByStationAndType(stationId, typeId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicles retrieved successfully")
                            .data(vehicles)
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

    @GetMapping("/search")
    public ResponseEntity<ResponseObject> searchAvailableVehicles(
            @RequestParam Long stationId,
            @RequestParam  LocalDate startDate,
            @RequestParam  LocalDate endDate) {
        try {

            VehicleAvailabilityResponse response = vehicleService.searchAvailableVehicles(stationId, startDate, endDate);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Vehicle availability retrieved successfully")
                            .data(response)
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
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

    @SecurityRequirement(name = "api")
    @GetMapping(value = "/station/{stationId}/generate-qr-codes", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Generate PDF with QR codes by station",
            description = "Generate a PDF file containing QR codes for vehicles in a specific station, grouped by vehicle type with optimized layout"
    )
    public ResponseEntity<byte[]> generateQRCodePDFByStation(@PathVariable Long stationId) {
        try {
            byte[] pdfBytes = vehicleService.generateQRCodePDFByStation(stationId);

            String filename = String.format("vehicle-qr-codes-station-%d.pdf", stationId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Station not found: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate PDF: " + e.getMessage()).getBytes());
        }
    }
}
