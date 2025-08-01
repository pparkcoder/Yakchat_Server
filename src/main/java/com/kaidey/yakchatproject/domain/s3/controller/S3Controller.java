package com.kaidey.yakchatproject.domain.s3.controller;

import com.kaidey.yakchatproject.domain.s3.dto.S3Dto;
import com.kaidey.yakchatproject.domain.s3.service.S3Service;
import com.kaidey.yakchatproject.domain.s3.service.S3ServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/api/s3")
@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3ServiceV2 s3ServiceV2;

    @PostMapping("/upload")
    public List<S3Dto> createPresignedUrl(@RequestParam List<String> files, @RequestParam Long userId) {
        return s3ServiceV2.generatePresignedPutUrl(files, userId);
    }

    @GetMapping("/download")
    public List<S3Dto> getPresignedUrl(@RequestParam List<String> keys) {
        return s3ServiceV2.generatePresignedGetUrl(keys);
    }
}
