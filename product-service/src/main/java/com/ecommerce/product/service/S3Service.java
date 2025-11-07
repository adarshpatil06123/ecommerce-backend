package com.ecommerce.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    /**
     * Upload a file to AWS S3
     * 
     * @param file The file to upload
     * @param folderName The folder name in S3 bucket
     * @return The public URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String folderName);
    
    /**
     * Delete a file from AWS S3
     * 
     * @param fileUrl The URL of the file to delete
     */
    void deleteFile(String fileUrl);
}
