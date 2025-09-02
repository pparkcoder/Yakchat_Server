package com.kaidey.yakchatproject.domain.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.kaidey.yakchatproject.domain.s3.dto.S3Dto;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceV2 {

    @Value("${cloud.aws.s3.bucket.default}")
    private String defaultBucket;

    @Value("${cloud.aws.s3.bucket.ocr}")
    private String ocrBucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public URL generatePresignedUrl(String objectKey, HttpMethod method) {
        // 이 과정이 없으면 오류
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(defaultBucket, objectKey)
                        .withMethod(method)
                        .withExpiration(createExpiraton());

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    // PutObject 요청에 대한 Presigned URL 생성
    public List<S3Dto> generatePresignedPutUrl(List<String> files, Long userId, String type) {
        List<S3Dto> result = new ArrayList<>();
        for (String file : files) {
            String path = type + "/" +  userId + "/" + file;
            URL url = generatePresignedUrl(path, HttpMethod.PUT);
            result.add(new S3Dto(path, url.toExternalForm()));
        }
        return result;
    }

    // GetObject 요청에 대한 Presigned URL 생성
    public List<S3Dto> generatePresignedGetUrl(List<String> keys) {
        List<S3Dto> result = new ArrayList<>();
        for (String key : keys) {
            URL url = generatePresignedUrl(key, HttpMethod.GET);
            result.add(new S3Dto(key, url.toExternalForm()));
        }
        return result;
    }

    // DeleteObject 요청에 대한 Presigned URL 생성
    public List<S3Dto> generatePresignedDeleteUrl(List<String> keys) {
        List<S3Dto> result = new ArrayList<>();
        for (String key : keys) {
            URL url = generatePresignedUrl(key, HttpMethod.DELETE);
            result.add(new S3Dto(key, url.toExternalForm()));
        }
        return result;
    }

    public String uploadDocumentImage(byte[] fileData,
                                      Long userId,
                                      String documentType,
                                      String fileName,
                                      String contentType) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        String key = String.format("documents/%d/%s/%s", userId, documentType, fileName);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(fileData.length);
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = "image/jpeg";
        }
        meta.setContentType(contentType);
        // 문서 이미지는 개인정보 → 기본 Private(ACL 설정 X)
        meta.addUserMetadata("document-type", documentType);
        meta.addUserMetadata("user-id", String.valueOf(userId));

        try {
            s3Client.putObject(ocrBucket, key, new ByteArrayInputStream(fileData), meta);
            return key;
        } catch (Exception e) {
            log.error("S3 문서 업로드 실패 - bucket: {}, key: {}", ocrBucket, key, e);
            throw e;
        }
    }

    // 접근 유효시간 설정
    private Date createExpiraton() {
        Date expTimeMillis = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 유효
        return expTimeMillis;
    }
}
