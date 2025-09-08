package com.kaidey.yakchatproject.domain.onboarding.controller;

import com.kaidey.yakchatproject.domain.onboarding.dto.*;
import com.kaidey.yakchatproject.domain.onboarding.entity.*;
import com.kaidey.yakchatproject.domain.onboarding.service.*;
import com.kaidey.yakchatproject.global.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {
    private final StudentOnboardingService studentSvc;
    private final ExpertOnboardingService expertSvc;


    @PutMapping("/student")
    public StudentOnboardingRes upsertStudent(@RequestBody @Valid StudentOnboardingReq req) {
        Long userId = AuthUtil.currentUserId();
        return studentSvc.upsert(userId, req);
    }


    @GetMapping("/student")
    public ResponseEntity<StudentProfile> getStudent() {
        Long userId = AuthUtil.currentUserId();
        return studentSvc.get(userId).map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @PutMapping("/expert")
    public ExpertOnboardingRes upsertExpert(@RequestBody @Valid ExpertOnboardingReq req) {
        Long userId = AuthUtil.currentUserId();
        return expertSvc.upsert(userId, req);
    }


    @GetMapping("/expert")
    public ResponseEntity<ExpertProfile> getExpert() {
        Long userId = AuthUtil.currentUserId();
        return expertSvc.get(userId).map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}