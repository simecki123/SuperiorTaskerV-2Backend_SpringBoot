package com.example.demo.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.demo.service.AmazonS3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

    private final AmazonS3 s3Client;

    private final String bucket;

    /**
     * Method that creates and updates if that is the case new image to amazon S3.
     * @param path string value. Hardcoded value that can be profile photos or group photos
     * @param fileName String value name of the file.
     * @param newFile Image file
     */
    @Override
    public void updateFileInS3(String path, String fileName, InputStream newFile) {
        String fullPath = path + "/" + fileName;
        try {
            if (s3Client.doesObjectExist(bucket, fullPath)) {
                s3Client.deleteObject(bucket, fullPath);
                log.info("Existing file deleted successfully");
            }

            s3Client.putObject(bucket, fullPath, newFile, new ObjectMetadata());
            log.info("New file uploaded successfully");
        } catch (AmazonServiceException e) {
            log.error("Error occurred while updating file in S3", e);
            throw new RuntimeException("Failed to update file in S3", e);
        }
    }

    /**
     * Method used in converter to create url of the image that is stored on amazon aws so frontend can use it.
     * @param fileUri Existing url od the image.
     * @return URL.
     */
    @Override
    public URL generatePresignedUrl(String fileUri) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileUri)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
