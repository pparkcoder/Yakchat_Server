package com.kaidey.yakchatproject.domain.email.controller;

import com.kaidey.yakchatproject.domain.email.dto.EmailDto;
import com.kaidey.yakchatproject.domain.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/email")
@RestController
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String>  sendAuthCode(
            @RequestBody @Valid EmailDto emailDto,
            @RequestHeader(value = "Temp-Token", required = false) String tempToken) {

//        emailService.sendEmailCode(emailDto);
        emailService.sendEmailCodeWithTempToken(emailDto, tempToken);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAuthCode(@RequestBody EmailDto emailDto) {
        emailService.verifyEmailCode(emailDto);
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }
}
