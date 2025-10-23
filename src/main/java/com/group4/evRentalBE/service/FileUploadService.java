package com.group4.evRentalBE.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    
    /**
     * Upload file to cloud storage
     * @param file the file to upload
     * @param folder the folder name (documents, vehicles, returns)
     * @return the uploaded file URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * Delete file from cloud storage
     * @param fileUrl the file URL to delete
     */
    void deleteFile(String fileUrl);
    
    /**
     * Upload multiple files
     * @param files the files to upload
     * @param folder the folder name
     * @return comma-separated URLs
     */
    String uploadMultipleFiles(MultipartFile[] files, String folder);
}
