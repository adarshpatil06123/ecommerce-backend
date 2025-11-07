package com.ecommerce.product.service;

import com.ecommerce.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "-" + System.currentTimeMillis() + "." + fileExtension;
            String key = folderName + "/" + fileName;

            log.info("Uploading file to S3: bucket={}, key={}", bucketName, key);

            // Build put request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            // Upload file
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Generate public URL
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3 error uploading file: {}", e.awsErrorDetails().errorMessage(), e);
            throw new BadRequestException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);
            
            if (key == null) {
                log.warn("Could not extract key from URL: {}", fileUrl);
                return;
            }

            log.info("Deleting file from S3: bucket={}, key={}", bucketName, key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", key);

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new BadRequestException("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Validate file size and type
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BadRequestException("File name is invalid");
        }

        String extension = getFileExtension(filename);
        boolean isValidExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new BadRequestException("File type not allowed. Allowed types: jpg, jpeg, png, gif, webp");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }

    /**
     * Extract S3 key from full URL
     */
    private String extractKeyFromUrl(String url) {
        if (url == null || !url.contains(bucketName)) {
            return null;
        }
        
        try {
            // URL format: https://bucket.s3.region.amazonaws.com/folder/file.jpg
            int bucketIndex = url.indexOf(bucketName);
            int keyStartIndex = url.indexOf("/", bucketIndex + bucketName.length());
            
            if (keyStartIndex != -1) {
                return url.substring(keyStartIndex + 1);
            }
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", e.getMessage());
        }
        
        return null;
    }
}
