package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.business.dto.response.CCCDInfo;
import com.group4.evRentalBE.business.dto.response.FPTAIResponse;
import com.group4.evRentalBE.infrastructure.exception.KYCException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OCRService {

    @Value("${fpt.ai.api-key}")
    private String apiKey;

    private static final String FPT_AI_API_URL = "https://api.fpt.ai/vision/idr/vnm";

    public CCCDInfo extractCCCD(MultipartFile image) {
        try {
            // Prepare multipart request
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add image file to the request
            ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
            
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("Calling FPT.AI API for CCCD extraction with file: {}", image.getOriginalFilename());
            
            ResponseEntity<FPTAIResponse> response = restTemplate.postForEntity(
                    FPT_AI_API_URL,
                    request,
                    FPTAIResponse.class
            );

            if (response.getBody() == null) {
                throw new KYCException("Không nhận được phản hồi từ FPT.AI API");
            }

            FPTAIResponse responseBody = response.getBody();
            
            // Check for API errors
            if (responseBody.getErrorCode() != null && !"0".equals(responseBody.getErrorCode())) {
                String errorMsg = responseBody.getErrorMessage() != null ? 
                    responseBody.getErrorMessage() : "Lỗi không xác định từ FPT.AI";
                log.error("FPT.AI API error: {} - {}", responseBody.getErrorCode(), errorMsg);
                throw new KYCException("Lỗi từ FPT.AI: " + errorMsg);
            }

            // Parse response và map sang CCCDInfo
            return mapToCCCDInfo(responseBody);
            
        } catch (KYCException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting CCCD information", e);
            throw new KYCException("Lỗi khi trích xuất thông tin CCCD: " + e.getMessage(), e);
        }
    }

    public CCCDInfo extractCCCDWithBothSides(MultipartFile frontImage, MultipartFile backImage) {
        try {
            log.info("Extracting CCCD with both sides - Front: {}, Back: {}", 
                frontImage.getOriginalFilename(), backImage.getOriginalFilename());
            
            // Extract front side
            CCCDInfo frontInfo = extractCCCD(frontImage);
            
            // Extract back side
            FPTAIResponse backResponse = extractBackSide(backImage);
            
            // Merge information from back side
            if (backResponse.getData() != null && !backResponse.getData().isEmpty()) {
                FPTAIResponse.CCCDData backData = backResponse.getData().get(0);
                
                // Set issue date and issued by from back side
                if (backData.getIssueDate() != null && !backData.getIssueDate().trim().isEmpty()) {
                    frontInfo.setIssueDate(backData.getIssueDate());
                }
                
                if (backData.getIssueLoc() != null && !backData.getIssueLoc().trim().isEmpty()) {
                    frontInfo.setIssuedBy(backData.getIssueLoc());
                }
            }
            
            log.info("Successfully merged CCCD information from both sides");
            return frontInfo;
            
        } catch (Exception e) {
            log.error("Error extracting CCCD with both sides", e);
            throw new KYCException("Lỗi khi trích xuất thông tin CCCD từ cả hai mặt: " + e.getMessage(), e);
        }
    }

    private FPTAIResponse extractBackSide(MultipartFile backImage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            ByteArrayResource fileResource = new ByteArrayResource(backImage.getBytes()) {
                @Override
                public String getFilename() {
                    return backImage.getOriginalFilename();
                }
            };
            
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("Calling FPT.AI API for back side extraction");
            
            ResponseEntity<FPTAIResponse> response = restTemplate.postForEntity(
                    FPT_AI_API_URL,
                    request,
                    FPTAIResponse.class
            );

            if (response.getBody() == null) {
                throw new KYCException("Không nhận được phản hồi từ FPT.AI API cho mặt sau");
            }

            FPTAIResponse responseBody = response.getBody();
            
            // Check for API errors
            if (responseBody.getErrorCode() != null && !"0".equals(responseBody.getErrorCode())) {
                log.warn("FPT.AI API warning for back side: {} - {}", 
                    responseBody.getErrorCode(), responseBody.getErrorMessage());
            }

            return responseBody;
            
        } catch (Exception e) {
            log.error("Error extracting back side information", e);
            throw new KYCException("Lỗi khi trích xuất thông tin mặt sau: " + e.getMessage(), e);
        }
    }

    private CCCDInfo mapToCCCDInfo(FPTAIResponse response) {
        if (response.getData() == null || response.getData().isEmpty()) {
            throw new KYCException("Không tìm thấy thông tin CCCD trong hình ảnh");
        }

        FPTAIResponse.CCCDData data = response.getData().get(0);
        CCCDInfo cccdInfo = new CCCDInfo();
        
        cccdInfo.setId(data.getId());
        cccdInfo.setFullName(data.getName());
        cccdInfo.setDateOfBirth(data.getDob());
        cccdInfo.setGender(data.getSex());
        cccdInfo.setNationality(data.getNationality());
        cccdInfo.setPlaceOfOrigin(data.getHome());
        cccdInfo.setPlaceOfResidence(data.getAddress());
        cccdInfo.setExpiryDate(data.getDoe());
        
        // Validate extracted data
        validateExtractedData(cccdInfo);
        
        return cccdInfo;
    }

    private void validateExtractedData(CCCDInfo cccdInfo) {
        if (cccdInfo.getId() == null || cccdInfo.getId().trim().isEmpty()) {
            throw new KYCException("Không trích xuất được số CCCD");
        }
        if (cccdInfo.getFullName() == null || cccdInfo.getFullName().trim().isEmpty()) {
            throw new KYCException("Không trích xuất được họ tên");
        }
        if (cccdInfo.getDateOfBirth() == null || cccdInfo.getDateOfBirth().trim().isEmpty()) {
            throw new KYCException("Không trích xuất được ngày sinh");
        }
    }
}
