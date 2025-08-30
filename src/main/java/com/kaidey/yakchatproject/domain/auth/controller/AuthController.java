package com.kaidey.yakchatproject.domain.auth.controller;

import com.kaidey.yakchatproject.domain.auth.dto.*;
import com.kaidey.yakchatproject.domain.auth.service.OcrVerificationService;
import com.kaidey.yakchatproject.domain.auth.dto.OcrStatusResponse;
import com.kaidey.yakchatproject.domain.auth.dto.OcrVerificationResponse;
import com.kaidey.yakchatproject.domain.email.service.EmailService;
import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.entity.UserType;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.OcrErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.global.util.RedisUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final OcrVerificationService ocrVerificationService;
    private final EmailService emailService;
    private final UserService userService;
    private final RedisUtil redisUtil;

    @PostMapping("/ocr-verify")
    public ResponseEntity<OcrVerificationResponse> verifyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String documentType) {
        OcrVerificationResponse resp = ocrVerificationService.verifyDocument(file, documentType);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/ocr-status/{tempToken}")
    public ResponseEntity<OcrStatusResponse> getOcrStatus(@PathVariable String tempToken) {
        return ResponseEntity.ok(ocrVerificationService.getOcrStatus(tempToken));
    }


    @PostMapping("/register")
    public ResponseEntity<User> completeRegistration(
            @RequestBody @Valid CompleteRegistrationRequest request,
            @RequestHeader("Temp-Token") String tempToken) {
        String verified = redisUtil.getData("email:verified:" + request.getEmail());
        if (!"true".equals(verified)) {
            throw new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED);  // ← 교체 권장
        }


        Map<String, Object> ocrData = ocrVerificationService.getOcrDataFromRedis(tempToken);
        UserDto userDto = createUserDtoFromOcrData(request, ocrData);
        User newUser = userService.registerUser(userDto);
        updateUserGradeFromOcrData(newUser, ocrData);
        saveDocumentImageToS3(newUser.getId(), ocrData);

        ocrVerificationService.deleteOcrTempData(tempToken);
        redisUtil.deleteData("email:verified:" + request.getEmail());
        return ResponseEntity.ok(newUser);
    }

    private UserDto createUserDtoFromOcrData(CompleteRegistrationRequest req, Map<String, Object> ocrData) {
        UserDto dto = new UserDto();
        dto.setNickname(req.getNickname());
        dto.setPassword(req.getPassword());
        dto.setEmail(req.getEmail());

        String docType = (String) ocrData.get("documentType");
        @SuppressWarnings("unchecked")
        Map<String, Object> f = (Map<String, Object>) ocrData.get("fields");

        String realName = null;
        if (f != null) {
            Object nameObj = f.get("name");
            if (nameObj instanceof String) {
                String s = ((String) nameObj).trim();
                if (!s.isEmpty()) realName = s;
            }
        }
        if (realName == null) {
            // name이 비어 있으면 가입 중단(에러코드는 프로젝트 규칙에 맞춰 교체 가능)
            throw new BusinessException(OcrErrorCode.INVALID_DOCUMENT_FORMAT.toErrorCode());
        }
        dto.setRealName(realName);

        // ✅ OCR 타입에 따라 userType 결정
        if ("student".equals(docType)) {
            dto.setUserType(UserType.STUDENT);
            dto.setSchool((String) f.getOrDefault("university", "미상"));
            dto.setGrade((String) f.getOrDefault("grade", "미상")); // 문자열 학년
        } else if ("professional".equals(docType)) {
            dto.setUserType(UserType.PROFESSIONAL);
            dto.setSchool("미상");
            dto.setGrade("미상"); // 전문가라 학년 없음, 기본값
        } else {
            // ✅ 혹시 모를 오염 데이터 방지
            throw new BusinessException(OcrErrorCode.INVALID_DOCUMENT_FORMAT.toErrorCode());
        }

        return dto;
    }

    private void updateUserGradeFromOcrData(User user, Map<String, Object> ocrData) {
        String docType = (String) ocrData.get("documentType");
        // 회원가입 시 등급 변동 없이 0,0 유지 (보너스를 원하면 아래 숫자 조정)
        userService.updateUserActivity(user, 0, 0);
    }


    private void saveDocumentImageToS3(Long userId, Map<String, Object> ocrData) {
        try {
            String base64 = (String) ocrData.get("originalImage");
            String documentType = (String) ocrData.get("documentType");
            String contentType = (String) ocrData.getOrDefault("contentType", "image/jpeg");
            if (base64 != null) {
                byte[] imageData = Base64.getDecoder().decode(base64);
                String s3Key = ocrVerificationService.saveImageToS3(imageData, userId, documentType, contentType);
                log.info("문서 이미지 S3 저장 완료 - user: {}, key: {}", userId, s3Key);
            }
        } catch (Exception e) {
            log.warn("이미지 S3 저장 실패 - userId: {}", userId, e);
        }
    }
}
