package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.response.CCCDInfo;
import com.group4.evRentalBE.business.dto.response.ErrorResponse;
import com.group4.evRentalBE.business.service.FileUploadService;
import com.group4.evRentalBE.business.service.impl.KYCService;
import com.group4.evRentalBE.business.service.impl.OCRService;
import com.group4.evRentalBE.domain.entity.KYCLog;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.infrastructure.exception.KYCException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC", description = "API quản lý KYC và OCR")
@SecurityRequirement(name = "api")
public class KYCController {

    private final OCRService ocrService;
    private final KYCService kycService;
    private final FileUploadService fileUploadService;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", 
        "image/jpg", 
        "image/png"
    );

    @PostMapping(value = "/extract-cccd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Trích xuất thông tin CCCD", description = "Upload ảnh CCCD (mặt trước và mặt sau) để trích xuất thông tin")
    public ResponseEntity<?> extractCCCD(
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam(value = "backImage", required = false) MultipartFile backImage) {
        try {
            log.info("Received request to extract CCCD information - Front: {}, Back: {}", 
                frontImage.getOriginalFilename(), 
                backImage != null ? backImage.getOriginalFilename() : "not provided");
            
            // Validate front image
            validateImage(frontImage, "frontImage");
            
            // Validate back image if provided
            if (backImage != null && !backImage.isEmpty()) {
                validateImage(backImage, "backImage");
            }

            CCCDInfo cccdInfo;
            String frontImageUrl;
            String backImageUrl = null;
            
            // Extract CCCD info
            if (backImage != null && !backImage.isEmpty()) {
                // Extract with both sides
                cccdInfo = ocrService.extractCCCDWithBothSides(frontImage, backImage);
                
                // Upload both images
                frontImageUrl = fileUploadService.uploadFile(frontImage, "kyc/front");
                backImageUrl = fileUploadService.uploadFile(backImage, "kyc/back");
            } else {
                // Extract with front side only
                cccdInfo = ocrService.extractCCCD(frontImage);
                
                // Upload front image only
                frontImageUrl = fileUploadService.uploadFile(frontImage, "kyc/front");
            }
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // Save KYC log to database with both image URLs
            KYCLog kycLog = kycService.saveKYCLog(currentUser, cccdInfo, frontImageUrl, backImageUrl);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("cccdInfo", cccdInfo);
            response.put("kycLogId", kycLog.getId());
            response.put("status", kycLog.getVerificationStatus());
            response.put("frontImageUrl", frontImageUrl);
            if (backImageUrl != null) {
                response.put("backImageUrl", backImageUrl);
            }

            log.info("Successfully extracted CCCD information for ID: {}", cccdInfo.getId());
            return ResponseEntity.ok(response);

        } catch (KYCException e) {
            log.error("KYC error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
                    
        } catch (Exception e) {
            log.error("Unexpected error during CCCD extraction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Lấy lịch sử KYC", description = "Lấy danh sách lịch sử KYC của người dùng hiện tại")
    public ResponseEntity<?> getKYCHistory() {
        try {
            User currentUser = getCurrentUser();
            List<KYCLog> history = kycService.getUserKYCHistory(currentUser);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting KYC history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy lịch sử KYC: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái KYC", description = "Kiểm tra trạng thái xác thực KYC của người dùng")
    public ResponseEntity<?> checkKYCStatus() {
        try {
            User currentUser = getCurrentUser();
            boolean hasApprovedKYC = kycService.hasApprovedKYC(currentUser);
            KYCLog latestKYC = kycService.getLatestKYCLog(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isVerified", hasApprovedKYC);
            response.put("latestKYC", latestKYC);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking KYC status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi kiểm tra trạng thái KYC: " + e.getMessage()));
        }
    }

    @PutMapping("/verify/{kycLogId}")
    @Operation(summary = "Xác thực KYC", description = "Admin xác thực hoặc từ chối KYC của người dùng")
    public ResponseEntity<?> verifyKYC(
            @PathVariable Long kycLogId,
            @RequestParam KYCLog.VerificationStatus status,
            @RequestParam(required = false) String notes) {
        try {
            KYCLog updatedLog = kycService.updateVerificationStatus(kycLogId, status, notes);
            return ResponseEntity.ok(updatedLog);
        } catch (Exception e) {
            log.error("Error verifying KYC", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi xác thực KYC: " + e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Lấy danh sách KYC chờ duyệt", description = "Admin lấy danh sách KYC chờ xác thực")
    public ResponseEntity<?> getPendingKYC() {
        try {
            List<KYCLog> pendingLogs = kycService.getPendingKYCLogs();
            return ResponseEntity.ok(pendingLogs);
        } catch (Exception e) {
            log.error("Error getting pending KYC", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi lấy danh sách KYC chờ duyệt: " + e.getMessage()));
        }
    }

    private void validateImage(MultipartFile image, String paramName) {
        if (image == null || image.isEmpty()) {
            throw new KYCException("Vui lòng upload ảnh " + paramName);
        }

        // Check file size
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new KYCException("Kích thước file " + paramName + " không được vượt quá 10MB");
        }

        // Check content type
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new KYCException("File " + paramName + " chỉ chấp nhận định dạng JPG, JPEG hoặc PNG");
        }

        // Check file extension
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new KYCException("Tên file " + paramName + " không hợp lệ hoặc không có phần mở rộng");
        }
    }

    private boolean hasValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") || 
               lowerCaseFilename.endsWith(".jpeg") || 
               lowerCaseFilename.endsWith(".png");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new KYCException("Vui lòng đăng nhập để sử dụng tính năng này");
        }
        return (User) authentication.getPrincipal();
    }
}
