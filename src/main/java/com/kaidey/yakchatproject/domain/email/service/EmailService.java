package com.kaidey.yakchatproject.domain.email.service;

import com.kaidey.yakchatproject.domain.email.dto.EmailDto;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.util.RedisUtil;
import com.kaidey.yakchatproject.domain.auth.service.OcrVerificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RedisUtil redisUtil;
    private final JavaMailSender mailSender;
    private final OcrVerificationService ocrVerificationService;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${redis.ttl.email-code}")      // 3분
    private long emailCodeTtlSeconds;

    @Autowired
    public EmailService(RedisUtil redisUtil, JavaMailSender mailSender,
                        OcrVerificationService ocrVerificationService, UserService userService) {
        this.redisUtil = redisUtil;
        this.mailSender = mailSender;
        this.ocrVerificationService = ocrVerificationService;
        this.userService = userService;
    }

    // 숫자 6자리 인증 코드 생성
    private String createdCode() {
        int leftLimit = 48;
        int rightLimit = 57;
        int codeLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(codeLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

    }

    public void sendEmailCodeWithTempToken(EmailDto emailDto, String tempToken) {
        if (tempToken != null && !tempToken.isBlank()) {
            Map<String, Object> ocrData = ocrVerificationService.getOcrDataFromRedis(tempToken);
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) ocrData.get("fields");
            String extractedName = (fields == null) ? null : (String) fields.get("name");
            if (extractedName != null && !extractedName.isBlank()) {
                // 개인화 이름을 3분 TTL로 캐시 (Email 본문에서 자동 사용)
                redisUtil.setDataExpire("email:displayname:" + emailDto.getEmail(),
                        extractedName, emailCodeTtlSeconds);
            }
        }
        sendEmailCode(emailDto);
    }

    // 이메일 내용 및 전송 설정
    private MimeMessage createEmailForm(String toEmail) throws MessagingException {
        String code = createdCode();
        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        message.setSubject("[PillChat]본인 인증 번호 안내드립니다.");

        String mailContect = "";
        mailContect += "<div style='margin:20px;'>";
        mailContect += "<h4>안녕하세요. <strong>PillChat</strong> 입니다.</h4>";
        mailContect += "<h4>인증번호는 발송된 시점부터 3분간만 유효하니 확인 후 바로 입력해 주시기 바랍니다.</h4>";
        mailContect += "<br>";
        mailContect += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        mailContect += "<div style='font-size:130%;margin-top:25px'>";
        mailContect += "인증번호 : <strong>";
        mailContect += code + "</strong><div><br/> "; // 메일에 인증번호 넣기
        mailContect += "</div>";
        message.setFrom(fromEmail);
        message.setText(mailContect, "utf-8", "html");

        redisUtil.setData(toEmail, code);

        return message;
    }



    // 인증 코드 전송
    public void sendEmailCode(EmailDto emailDto) {
        try {
            String toEmail = emailDto.getEmail();
            if (userService.existsByEmail(toEmail)) {
                throw new BusinessException(UserErrorCode.ALREADY_EXIST_EMAIL);
            }
            if (redisUtil.existData(toEmail)) {
                redisUtil.deleteData(toEmail);
            }
            mailSender.send(createEmailForm(toEmail));
        } catch (MessagingException e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    // 인증 코드 검증
    public void verifyEmailCode(EmailDto emailDto) {
        String userEmail = emailDto.getEmail();
        String userCode = emailDto.getCode();
        if (userCode == null || !userCode.equals(redisUtil.getData(userEmail))) {
            throw new BusinessException(CommonErrorCode.INVALID_EMAIL_CODE);
        }
        redisUtil.setDataExpire("email:verified:" + userEmail, "true", emailCodeTtlSeconds);
        redisUtil.deleteData(userEmail);

    }
}
