package com.kaidey.yakchatproject.domain.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.kaidey.yakchatproject.domain.s3.dto.S3Dto;
import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    // 이미지 업로드 용
    public List<S3Dto> createPresignedUrl(Long userId, List<String> fileNames) {
        List<S3Dto> result = new ArrayList<>();

        for (String fileName : fileNames) {
            //String uuid = UUID.randomUUID().toString();
            String path = "profile/" + userId + "/" + fileName;

            GeneratePresignedUrlRequest generatePresignedUrlRequest = createGeneratePresignedUrlRequest(path);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            result.add(new S3Dto(path, url.toExternalForm()));
        }

        return result;
    }

    // Presigned URL 발급 요청 객체 생성
    private GeneratePresignedUrlRequest createGeneratePresignedUrlRequest(String fileName) {
        GeneratePresignedUrlRequest request  = new GeneratePresignedUrlRequest(bucket, fileName)
                .withMethod(HttpMethod.PUT)
                .withKey(fileName)
                .withExpiration(createExpiraton());

        request.addRequestParameter(
                Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString());

        return request;
    }

    // 이미지 조회 용
    public List<S3Dto> getPresignedUrl(List<String> keys) {
        List<S3Dto> result = new ArrayList<>();
        for (String key : keys) {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePresignedUrlRequest(key);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            result.add(new S3Dto(key, url.toExternalForm()));
        }
        return result;
    }

    // Presigned URL 발급 요청 객체 생성
    private GeneratePresignedUrlRequest getGeneratePresignedUrlRequest(String path) {
        GeneratePresignedUrlRequest request  = new GeneratePresignedUrlRequest(bucket, path)
                .withMethod(HttpMethod.GET)
                .withExpiration(createExpiraton());

        return request;
    }

    // 접근 유효시간 설정
    private Date createExpiraton() {
        Date expTimeMillis = new Date(System.currentTimeMillis() + 1000 * 60 * 10); // 10분 유효
        return expTimeMillis;
    }
}
