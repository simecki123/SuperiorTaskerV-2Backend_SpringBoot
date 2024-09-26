package com.example.demo.service.impl;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.demo.service.AmazonS3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

    @Override
    public PutObjectResult uploadToS3(String path, String fileName, InputStream file) {
        return null;
    }

    @Override
    public byte[] downloadFromS3(String fileUri) throws IOException {
        return new byte[0];
    }

    @Override
    public void deleteFromS3(String path, String fileName) {

    }

    @Override
    public void updateFileInS3(String path, String fileName, InputStream newFile) {

    }

    @Override
    public URL generatePresignedUrl(String fileUri) {
        return null;
    }
}
