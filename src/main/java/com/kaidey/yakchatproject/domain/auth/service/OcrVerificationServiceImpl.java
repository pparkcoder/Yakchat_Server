package com.kaidey.yakchatproject.domain.auth.service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaidey.yakchatproject.domain.auth.dto.OcrFieldsDto;
import com.kaidey.yakchatproject.domain.auth.dto.OcrStatusResponse;
import com.kaidey.yakchatproject.domain.auth.dto.OcrVerificationResponse;
import com.kaidey.yakchatproject.domain.s3.service.S3ServiceV2;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.OcrErrorCode;
import com.kaidey.yakchatproject.global.util.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrVerificationServiceImpl implements OcrVerificationService {

	private final RestTemplate restTemplate;
	private final RedisUtil redisUtil;
	private final S3ServiceV2 s3Service;
	private final ObjectMapper objectMapper;
	private final OcrValidationService ocrValidationService;

	@Value("${ocr.service.url:http://localhost:8000}")
	private String ocrServiceUrl;

	@Value("${ocr.internal.token:}")
	private String ocrInternalToken;

	@Value("${redis.ttl.ocr-temp:600}")
	private long ocrTempTtl;

	private static final String OCR_TEMP_PREFIX = "ocr:temp:";

	// 문서 인증 및 임시 토큰 발급
	@Override
	public OcrVerificationResponse verifyDocument(MultipartFile file, String documentType) {
		try {
			validateDocumentType(documentType);
			validateImageFile(file);

			JsonNode ocrResult = callOcrApi(file, documentType);
			if (!ocrResult.path("valid").asBoolean(false)) {
				String message = ocrResult.path("message").asText("문서 인증에 실패했습니다");
				return new OcrVerificationResponse(false, null, documentType, null, message);
			}

			// 필드 추출
			OcrFieldsDto fields = extractFieldsFromOcrResult(ocrResult, documentType);

			// 자격 검증 강화 (키워드 + 필수 필드)
			validatePharmacyQualification(ocrResult, documentType);

			// Temp 저장 (originalImage, contentType 포함)
			String tempToken = saveOcrResultToRedis(
				ocrResult, documentType, file.getBytes(), fields, file.getContentType());

			String msg = String.format("%s님의 %s 인증이 완료되었습니다", fields.getName(),
				documentType.equals("student") ? "학생증" : "면허증");
			return new OcrVerificationResponse(true, tempToken, documentType, fields, msg);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("verifyDocument 실패", e);
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		}
	}

	// OCR API 호출
	private JsonNode callOcrApi(MultipartFile file, String documentType) {
		try {
			String endpoint = documentType.equals("student") ? "/ocr/student" : "/ocr/professional";
			String url = ocrServiceUrl + endpoint;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			if (ocrInternalToken != null && !ocrInternalToken.isBlank())
				headers.setBearerAuth(ocrInternalToken);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			});

			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			if (response.getStatusCode() == HttpStatus.OK)
				return objectMapper.readTree(response.getBody());
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		} catch (IOException e) {
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		}
	}

	// OCR 결과에서 필드 추출
	private OcrFieldsDto extractFieldsFromOcrResult(JsonNode ocrResult, String documentType) {
		OcrFieldsDto f = new OcrFieldsDto();
		JsonNode n = ocrResult.path("fields");
		if ("student".equals(documentType)) {
			f.setName(n.path("name").asText(""));
			f.setStudentId(n.path("studentId").asText(""));
			f.setUniversity(n.path("university").asText(""));
			f.setDepartment(n.path("department").asText(""));
		} else {
			f.setName(n.path("name").asText(""));
			f.setLicenseNumber(n.path("licenseNumber").asText(""));
			f.setIssueDate(n.path("issueDate").asText(""));
		}
		return f;
	}

	private String saveOcrResultToRedis(JsonNode ocrResult, String documentType,
		byte[] imageData, OcrFieldsDto fields, String contentType) {
		String tempToken = UUID.randomUUID().toString();
		Map<String, Object> tempData = new HashMap<>();
		tempData.put("documentType", documentType);
		tempData.put("valid", ocrResult.path("valid").asBoolean());
		tempData.put("fields", objectMapper.convertValue(fields, Map.class));
		tempData.put("originalImage", Base64.getEncoder().encodeToString(imageData));
		tempData.put("contentType",
			(contentType != null && contentType.startsWith("image/")) ? contentType : "image/jpeg");
		tempData.put("timestamp", System.currentTimeMillis());
		tempData.put("fileName", generateFileName(documentType, (String)tempData.get("contentType")));

		try {
			String dataJson = objectMapper.writeValueAsString(tempData);
			String key = OCR_TEMP_PREFIX + tempToken;
			long ttl = ocrTempTtl; // ← @Value("${redis.ttl.ocr-temp:600}") 로 주입
			redisUtil.setDataExpire(key, dataJson, ttl);
			return tempToken;
		} catch (Exception e) {
			log.error("Failed to save OCR temp data to Redis: key={}, ttl={}", tempToken, ocrTempTtl, e);
			throw new BusinessException(OcrErrorCode.OCR_SERVICE_UNAVAILABLE.toErrorCode());
		}
	}

	@Override
	public Map<String, Object> getOcrDataFromRedis(String tempToken) {
		String key = OCR_TEMP_PREFIX + tempToken;
		String dataJson = redisUtil.getData(key);
		if (dataJson == null)
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		try {
			Map<String, Object> data = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {
			});
			// 남은 만료시간 확인 (Redis TTL 신뢰)
			long ttl = redisUtil.getExpire(key);
			if (ttl <= 0) {
				redisUtil.deleteData(key);
				throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
			}
			return data;
		} catch (Exception e) {
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		}
	}

	@Override
	public OcrStatusResponse getOcrStatus(String tempToken) {
		String key = OCR_TEMP_PREFIX + tempToken;
		Map<String, Object> ocrData = getOcrDataFromRedis(tempToken);
		String docType = (String)ocrData.get("documentType");
		Boolean verified = (Boolean)ocrData.get("valid");
		long expiresIn = Math.max(0, redisUtil.getExpire(key));
		OcrFieldsDto fields = objectMapper.convertValue(ocrData.get("fields"), OcrFieldsDto.class);
		return new OcrStatusResponse(Boolean.TRUE.equals(verified), docType, fields, expiresIn, fields.getName());
	}

	@Override
	public String saveImageToS3(byte[] imageData, Long userId, String documentType, String contentType) {
		String fileName = generateFileName(documentType, contentType);
		return s3Service.uploadDocumentImage(imageData, userId, documentType, fileName, contentType);
	}

	@Override
	public void deleteOcrTempData(String tempToken) {
		redisUtil.deleteData(OCR_TEMP_PREFIX + tempToken);
	}

	private void validateDocumentType(String documentType) {
		if (!"student".equals(documentType) && !"professional".equals(documentType))
			throw new BusinessException(OcrErrorCode.INVALID_DOCUMENT_FORMAT.toErrorCode());
	}

	private void validateImageFile(MultipartFile file) {
		if (file.isEmpty())
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		if (file.getSize() > 10 * 1024 * 1024)
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/"))
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
	}

	private void validatePharmacyQualification(JsonNode ocrResult, String documentType) {
		String fullText = ocrResult.path("text").asText("");
		Map<String, Object> fields = objectMapper.convertValue(ocrResult.path("fields"),
			new TypeReference<Map<String, Object>>() {
			});
		boolean ok;
		if ("student".equals(documentType)) {
			ok = ocrValidationService.isValidStudentCard(fullText, fields);
		} else {
			ok = ocrValidationService.isValidProfessionalLicense(fullText, fields);
		}
		if (!ok)
			throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());
	}

	// 파일명 생성 (UUID + timestamp + 확장자)
	private String generateFileName(String documentType, String contentType) {
		String ext = "jpg";
		if ("image/png".equalsIgnoreCase(contentType))
			ext = "png";
		else if ("image/webp".equalsIgnoreCase(contentType))
			ext = "webp";
		return String.format("%s_%s_%d.%s",
			documentType, UUID.randomUUID().toString().substring(0, 8), System.currentTimeMillis(), ext);
	}
}
