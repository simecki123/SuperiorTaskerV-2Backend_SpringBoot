package com.example.demo.service;

import java.io.InputStream;
import java.net.URL;

public interface AmazonS3Service {
    void updateFileInS3(String path, String fileName, InputStream newFile);
    URL generatePresignedUrl(String fileUri);
}
