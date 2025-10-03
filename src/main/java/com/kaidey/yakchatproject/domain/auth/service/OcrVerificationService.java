package com.kaidey.yakchatproject.domain.auth.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.kaidey.yakchatproject.domain.auth.dto.OcrStatusResponse;
import com.kaidey.yakchatproject.domain.auth.dto.OcrVerificationResponse;

public interface OcrVerificationService {

	OcrVerificationResponse verifyDocument(MultipartFile file, String documentType);

	Map<String, Object> getOcrDataFromRedis(String tempToken);

	OcrStatusResponse getOcrStatus(String tempToken);

	String saveImageToS3(byte[] imageData, Long userId, String documentType, String contentType);

	void deleteOcrTempData(String tempToken);

}
