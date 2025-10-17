package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.RentalStationResponse;
import com.group4.evRentalBE.service.RentalStationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rental-stations")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class RentalStationController {

    private final RentalStationService rentalStationService;

    @PostMapping
    public ResponseEntity<ResponseObject> createRentalStation(
            @Valid @RequestBody RentalStationRequest rentalStationRequest) {
        try {
            RentalStationResponse response = rentalStationService.createRentalStation(rentalStationRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Rental station created successfully")
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
    public ResponseEntity<ResponseObject> getAllRentalStations() {
        try {
            List<RentalStationResponse> rentalStations = rentalStationService.getAllRentalStations();
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Rental stations retrieved successfully")
                            .data(rentalStations)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve rental stations: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getRentalStationById(@PathVariable Long id) {
        try {
            RentalStationResponse rentalStation = rentalStationService.getRentalStationById(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Rental station retrieved successfully")
                            .data(rentalStation)
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
    public ResponseEntity<ResponseObject> updateRentalStation(
            @PathVariable Long id,
            @Valid @RequestBody RentalStationRequest rentalStationRequest) {
        try {
            RentalStationResponse updatedRentalStation = rentalStationService.updateRentalStation(id, rentalStationRequest);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Rental station updated successfully")
                            .data(updatedRentalStation)
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
    public ResponseEntity<ResponseObject> deleteRentalStation(@PathVariable Long id) {
        try {
            rentalStationService.deleteRentalStation(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Rental station deleted successfully")
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

    // Additional endpoints
    @GetMapping("/city/{city}")
    public ResponseEntity<ResponseObject> getRentalStationsByCity(@PathVariable String city) {
        try {
            List<RentalStationResponse> rentalStations = rentalStationService.getRentalStationsByCity(city);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Rental stations retrieved successfully")
                            .data(rentalStations)
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


}