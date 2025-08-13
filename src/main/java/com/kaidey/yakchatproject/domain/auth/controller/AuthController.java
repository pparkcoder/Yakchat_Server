package com.kaidey.yakchatproject.domain.auth.controller;

import com.kaidey.yakchatproject.domain.auth.dto.*;
import com.kaidey.yakchatproject.domain.auth.service.OcrVerificationService;
import com.kaidey.yakchatproject.domain.email.dto.EmailDto;
import com.kaidey.yakchatproject.domain.email.service.EmailService;
import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.OcrErrorCode;
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


    @PostMapping("/register-complete")
    public ResponseEntity<User> completeRegistration(
            @RequestBody @Valid CompleteRegistrationRequest request,
            @RequestHeader("Temp-Token") String tempToken) {
        String verified = redisUtil.getData("email:verified:" + request.getEmail());
        if (!"true".equals(verified)) throw new BusinessException(OcrErrorCode.TEMP_TOKEN_NOT_FOUND.toErrorCode());


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
        dto.setUsername(req.getUsername());
        dto.setPassword(req.getPassword());
        String docType = (String) ocrData.get("documentType");
        @SuppressWarnings("unchecked") Map<String, Object> f = (Map<String, Object>) ocrData.get("fields");
        if ("student".equals(docType)) {
            dto.setSchool((String) f.get("university"));
            dto.setGrade("약학과 학생");
            dto.setAge(22);
        } else {
            dto.setSchool("약사");
            dto.setGrade("전문 약사");
            dto.setAge(30);
        }
        return dto;
    }

    private void updateUserGradeFromOcrData(User user, Map<String, Object> ocrData) {
        String docType = (String) ocrData.get("documentType");
        if ("student".equals(docType)) {
            userService.updateUserActivity(user, 0, 0, 0, 0, 0);
        } else {
            userService.updateUserActivity(user, 5, 2, 10, 1, 0);
        }
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
