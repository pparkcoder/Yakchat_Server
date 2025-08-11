package com.kaidey.yakchatproject.domain.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.kaidey.yakchatproject.domain.s3.dto.S3Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ServiceV2 {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public URL generatePresignedUrl(String objectKey, HttpMethod method) {
        // 이 과정이 없으면 오류
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
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

    // 접근 유효시간 설정
    private Date createExpiraton() {
        Date expTimeMillis = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 유효
        return expTimeMillis;
    }
}
