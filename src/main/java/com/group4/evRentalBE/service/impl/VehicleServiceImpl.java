package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.VehicleMapper;
import com.group4.evRentalBE.model.dto.request.VehicleRequest;
import com.group4.evRentalBE.model.dto.response.VehicleAvailabilityResponse;
import com.group4.evRentalBE.model.dto.response.VehicleResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.VehicleRepository;
import com.group4.evRentalBE.repository.VehicleTypeRepository;
import com.group4.evRentalBE.repository.RentalStationRepository;
import com.group4.evRentalBE.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final RentalStationRepository rentalStationRepository;
    private final VehicleMapper vehicleMapper;
    private final QRCodeServiceImpl qrCodeService;



    @Override
    public byte[] generateQRCodePDFByStation(Long stationId) throws Exception {
        // Validate station exists
        RentalStation station = rentalStationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental station not found with id: " + stationId));

        // Get vehicles by station and group by type
        List<Vehicle> vehicles = vehicleRepository.findByStationId(stationId);
        Map<VehicleType, List<Vehicle>> vehiclesByType = vehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getType));

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDFont font = loadUnicodeFont(document);

            // Process each vehicle type
            for (Map.Entry<VehicleType, List<Vehicle>> entry : vehiclesByType.entrySet()) {
                VehicleType vehicleType = entry.getKey();
                List<Vehicle> typeVehicles = entry.getValue();

                // Create a new page for each vehicle type
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Add station and vehicle type header
                    addStationHeader(contentStream, font, station, vehicleType);

                    // Add QR codes in grid layout
                    addQRCodeGrid(contentStream, document, font, typeVehicles);
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
    private void addStationHeader(PDPageContentStream contentStream, PDFont font,
                                  RentalStation station, VehicleType vehicleType) throws IOException {
        contentStream.setFont(font, 14);
        contentStream.beginText();

        // Station information
        contentStream.newLineAtOffset(50, 750);
        safeShowText(contentStream, "Station: " + station.getCity() + " - " + station.getAddress());

        // Vehicle type information
        contentStream.newLineAtOffset(0, -25);
        safeShowText(contentStream, "Vehicle Type: " + vehicleType.getName());
        contentStream.newLineAtOffset(0, -25);
        safeShowText(contentStream, "Rental Rate: $" + vehicleType.getRentalRate() + "/day");
        contentStream.newLineAtOffset(0, -25);
        safeShowText(contentStream, "Deposit: $" + vehicleType.getDepositAmount());

        contentStream.endText();
    }

    /**
     * Add QR codes in an optimized grid layout
     */
    private void addQRCodeGrid(PDPageContentStream contentStream, PDDocument document,
                               PDFont font, List<Vehicle> vehicles) throws Exception {
        int qrSize = 120; // Smaller QR code size for grid layout
        int margin = 50;
        int startY = 650; // Start below the header
        int maxColumns = 3; // 3 QR codes per row
        int verticalSpacing = 140; // Space between rows

        int currentX = margin;
        int currentY = startY;
        int itemCount = 0;

        for (Vehicle vehicle : vehicles) {
            // Check if we need to move to next row
            if (itemCount % maxColumns == 0 && itemCount > 0) {
                currentX = margin;
                currentY -= verticalSpacing;

                // Check if we need a new page
                if (currentY < 100) {
                    // Start new page for remaining vehicles
                    contentStream.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);
                    currentY = 750;
                    addContinuationHeader(contentStream, font, "Continued - " + vehicle.getType().getName());
                }
            }

            // Generate QR code
            String qrContent = String.valueOf(vehicle.getId());
            byte[] qrImageBytes = qrCodeService.generateQRCodeImage(qrContent, 200, 200);
            PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrImageBytes, "QRCode");

            // Draw QR code
            contentStream.drawImage(qrImage, currentX, currentY - qrSize, qrSize, qrSize);

            // Add vehicle information below QR code
            contentStream.setFont(font, 8);
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX, currentY - qrSize - 15);
            safeShowText(contentStream, "ID: " + vehicle.getId());
            contentStream.newLineAtOffset(0, -12);
            safeShowText(contentStream, "Status: " + vehicle.getStatus());
            contentStream.endText();

            // Move to next column
            currentX += qrSize + 50; // 50px spacing between QR codes
            itemCount++;
        }
    }

    /**
     * Add continuation header for additional pages
     */
    private void addContinuationHeader(PDPageContentStream contentStream, PDFont font, String title) throws IOException {
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 750);
        safeShowText(contentStream, title);
        contentStream.endText();
    }

    // Các phương thức loadUnicodeFont, safeShowText, convertVietnameseToAscii giữ nguyên
    private PDFont loadUnicodeFont(PDDocument document) throws IOException {
        try {
            // Try to load a system font that supports Vietnamese
            // First, try to use Arial (common on Windows)
            try {
                ClassPathResource resource = new ClassPathResource("fonts/arial.ttf");
                if (resource.exists()) {
                    try (InputStream fontStream = resource.getInputStream()) {
                        return PDType0Font.load(document, fontStream);
                    }
                }
            } catch (Exception e) {
                // Continue to next attempt
            }

            // Try to load from system fonts
            String[] fontPaths = {
                    "C:/Windows/Fonts/arial.ttf",           // Windows
                    "/System/Library/Fonts/Arial.ttf",      // macOS
                    "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", // Linux
                    "/usr/share/fonts/TTF/arial.ttf"        // Linux alternative
            };

            for (String fontPath : fontPaths) {
                try {
                    java.io.File fontFile = new java.io.File(fontPath);
                    if (fontFile.exists()) {
                        return PDType0Font.load(document, fontFile);
                    }
                } catch (Exception e) {
                    // Continue to next font path
                }
            }

            // If no system font is found, use built-in font with ASCII fallback
            return PDType0Font.load(document,
                    getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf"));

        } catch (Exception e) {
            throw new RuntimeException("Could not load a Unicode-supporting font.", e);
        }
    }

    private void safeShowText(PDPageContentStream contentStream, String text) throws IOException {
        try {
            contentStream.showText(text);
        } catch (Exception e) {
            // If Unicode text fails, convert to ASCII approximation
            String asciiText = convertVietnameseToAscii(text);
            contentStream.showText(asciiText);
        }
    }

    private String convertVietnameseToAscii(String text) {
        // Basic Vietnamese to ASCII conversion
        return text
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[ÌÍỊỈĨ]", "I")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[ỲÝỴỶỸ]", "Y")
                .replaceAll("[đ]", "d")
                .replaceAll("[Đ]", "D");
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest vehicleRequest) {
        // Validate vehicle type exists
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleRequest.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + vehicleRequest.getTypeId()));

        // Validate rental station exists
        RentalStation rentalStation = rentalStationRepository.findById(vehicleRequest.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + vehicleRequest.getStationId()));

        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequest, vehicleType, rentalStation);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(savedVehicle);
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest vehicleRequest) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Validate và cập nhật relationships
        if (vehicleRequest.getTypeId() != null) {
            VehicleType vehicleType = vehicleTypeRepository.findById(vehicleRequest.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + vehicleRequest.getTypeId()));
            vehicle.setType(vehicleType);
        }

        if (vehicleRequest.getStationId() != null) {
            RentalStation rentalStation = rentalStationRepository.findById(vehicleRequest.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + vehicleRequest.getStationId()));
            vehicle.setStation(rentalStation);
        }

        if (vehicleRequest.getStatus() != null) {
            vehicle.updateStatus(vehicleRequest.getStatus());
        }

        // Cập nhật các trường khác
        if (vehicleRequest.getConditionNotes() != null) {
            vehicle.setConditionNotes(vehicleRequest.getConditionNotes());
        }
        if (vehicleRequest.getPhotos() != null) {
            vehicle.setPhotos(vehicleRequest.getPhotos());
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(updatedVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Check if vehicle has active contracts
        if (vehicle.getContracts() != null) {
            throw new ConflictException("Cannot delete vehicle. There is an active contract associated with this vehicle.");
        }

        vehicleRepository.delete(vehicle);
    }

    @Override
    public List<VehicleResponse> getVehiclesByStation(Long stationId) {
        List<Vehicle> vehicles = vehicleRepository.findByStationId(stationId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleResponse> getVehiclesByType(Long typeId) {
        List<Vehicle> vehicles = vehicleRepository.findByTypeId(typeId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }



    @Override
    public List<VehicleResponse> getVehiclesByStationAndType(Long stationId, Long typeId) {
        // Validate station exists
        if (!rentalStationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("RentalStation not found with id: " + stationId);
        }

        // Validate vehicle type exists
        if (!vehicleTypeRepository.existsById(typeId)) {
            throw new ResourceNotFoundException("VehicleType not found with id: " + typeId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByStationIdAndTypeId(stationId, typeId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleAvailabilityResponse searchAvailableVehicles(Long stationId, LocalDate startDate, LocalDate endDate) {
        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Validate station exists
        RentalStation station = rentalStationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + stationId));

        List<Vehicle> allAvailableVehicles = vehicleRepository.findAvailableVehiclesByStation(
                stationId,
                startDate ,
                endDate
        );

        // Group vehicles by type
        Map<VehicleType, List<Vehicle>> vehiclesByType = allAvailableVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getType));

        // Build response for each vehicle type
        List<VehicleAvailabilityResponse.VehicleTypeAvailability> vehicleTypeAvailabilities = 
                vehiclesByType.entrySet().stream()
                .map(entry -> {
                    VehicleType type = entry.getKey();
                    List<Vehicle> vehicles = entry.getValue();
                    
                    // Count total vehicles of this type at station
                    long totalVehicles = vehicleRepository.countByStationAndType(
                            stationId,
                            type.getId()
                    );
                    
                    // Map to VehicleResponse
                    List<VehicleResponse> vehicleResponses = vehicles.stream()
                            .map(vehicleMapper::toResponse)
                            .collect(Collectors.toList());
                    
                    return VehicleAvailabilityResponse.VehicleTypeAvailability.builder()
                            .typeId(type.getId())
                            .typeName(type.getName())
                            .depositAmount(type.getDepositAmount())
                            .rentalRate(type.getRentalRate())
                            .totalVehicles((int) totalVehicles)
                            .availableCount(vehicles.size())
                            .availableVehicles(vehicleResponses)
                            .build();
                })
                .sorted((a, b) -> a.getTypeId().compareTo(b.getTypeId()))
                .collect(Collectors.toList());

        return VehicleAvailabilityResponse.builder()
                .stationId(station.getId())
                .stationName(station.getCity() + " - " + station.getAddress())
                .searchStartDate(startDate)
                .searchEndDate(endDate)
                .vehicleTypes(vehicleTypeAvailabilities)
                .build();
    }
}
