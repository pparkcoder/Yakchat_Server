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
    public ResponseEntity<String> sendAuthCode(@RequestBody @Valid EmailDto emailDto) {
        try {
            emailService.sendEmailCode(emailDto);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증번호 전송이 실패하였습니다.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAuthCode(@RequestBody EmailDto emailDto) {
        if(emailService.verifyEmailCode(emailDto)){
            return ResponseEntity.ok("인증이 완료되었습니다.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않습니다.");
    }

}
