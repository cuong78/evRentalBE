package com.group4.evRentalBE.business.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group4.evRentalBE.infrastructure.exception.exceptions.FileStorageException;
import com.group4.evRentalBE.business.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Validate file
            validateFile(file);
            
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ev-rental/" + folder,
                            "public_id", fileName,
                            "resource_type", "auto"
                    ));
            
            String fileUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) return;
            
            // Extract public_id from URL
            String publicId = extractPublicId(fileUrl);
            
            // Delete from Cloudinary
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted: {} - Result: {}", publicId, result.get("result"));
            
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String uploadMultipleFiles(MultipartFile[] files, String folder) {
        if (files == null || files.length == 0) {
            return null;
        }
        
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = uploadFile(file, folder);
                urls.add(url);
            }
        }
        
        return String.join(",", urls);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }
        
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileStorageException("File size exceeds maximum limit (10MB)");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Only image files are allowed");
        }
    }

    private String extractPublicId(String fileUrl) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/xxx/image/upload/v123/ev-rental/documents/file.jpg
        // -> ev-rental/documents/file
        
        String[] parts = fileUrl.split("/upload/");
        if (parts.length < 2) return fileUrl;
        
        String pathAfterUpload = parts[1];
        String[] pathParts = pathAfterUpload.split("/");
        
        // Skip version number (v123...)
        StringBuilder publicId = new StringBuilder();
        for (int i = 1; i < pathParts.length; i++) {
            if (i > 1) publicId.append("/");
            publicId.append(pathParts[i].split("\\.")[0]); // Remove extension
        }
        
        return publicId.toString();
    }
}
