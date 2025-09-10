package com.kaidey.yakchatproject.domain.onboarding.controller;

import com.kaidey.yakchatproject.domain.onboarding.dto.*;
import com.kaidey.yakchatproject.domain.onboarding.entity.*;
import com.kaidey.yakchatproject.domain.onboarding.service.*;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.domain.user.entity.UserType;
import org.springframework.web.server.ResponseStatusException;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.global.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {
    private final StudentOnboardingService studentSvc;
    private final ExpertOnboardingService expertSvc;
    private final UserService userService;


    @PutMapping("/student")
    public StudentOnboardingRes upsertStudent(@RequestBody @Valid StudentOnboardingReq req) {
        Long userId = AuthUtil.currentUserId();
        User user = userService.getUserById(userId);
        if (user.getUserType() != UserType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can access this API");
        }
        return studentSvc.upsert(userId, req);
    }


    @GetMapping("/student")
    public ResponseEntity<StudentProfileRes> getStudent() {
        Long userId = AuthUtil.currentUserId();
        return studentSvc.get(userId)
                .map(p -> ResponseEntity.ok(new StudentProfileRes(
                        p.getUserId(),
                        p.getGrade(),
                        p.getAge(),
                        p.getStudyDays(),
                        p.getStudyTimes(),
                        p.getWeakSubjects(),
                        p.getStrongSubjects(),
                        p.getCourses(),
                        p.getVersion()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @PutMapping("/professional")
    public ExpertOnboardingRes upsertExpert(@RequestBody @Valid ExpertOnboardingReq req) {
        Long userId = AuthUtil.currentUserId();
        User user = userService.getUserById(userId);
        if (user.getUserType() != UserType.PROFESSIONAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professionals can access this API");
        }
        return expertSvc.upsert(userId, req);
    }

    @GetMapping("/professional")
    public ResponseEntity<ExpertProfileRes> getExpert() {
        Long userId = AuthUtil.currentUserId();
        User user = userService.getUserById(userId);
        if (user.getUserType() != UserType.PROFESSIONAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only professionals can access this API");
        }

        return expertSvc.get(userId)
                .map(p -> ResponseEntity.ok(new ExpertProfileRes(
                        p.getUserId(),
                        p.getJob(),
                        p.getWorkplace(),
                        p.getStrongSubjects(),
                        p.getAvailableDays(),
                        p.getAvailableTimes(),
                        p.getAnswerCycle(),
                        p.getAvgAnswerCount(),
                        p.getVersion()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @GetMapping("/me")
    public ResponseEntity<OnboardingMeRes> getMyOnboarding() {
        Long userId = AuthUtil.currentUserId();
        User user = userService.getUserById(userId);

        if (user.getUserType() == UserType.STUDENT) {
            return studentSvc.get(userId)
                    .map(p -> new OnboardingMeRes(
                            UserType.STUDENT,
                            new StudentProfileRes(
                                    p.getUserId(),
                                    p.getGrade(),
                                    p.getAge(),
                                    p.getStudyDays(),
                                    p.getStudyTimes(),
                                    p.getWeakSubjects(),
                                    p.getStrongSubjects(),
                                    p.getCourses(),
                                    p.getVersion()
                            ),
                            null
                    ))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else { // PROFESSIONAL
            return expertSvc.get(userId)
                    .map(p -> new OnboardingMeRes(
                            UserType.PROFESSIONAL,
                            null,
                            new ExpertProfileRes(
                                    p.getUserId(),
                                    p.getJob(),
                                    p.getWorkplace(),
                                    p.getStrongSubjects(),
                                    p.getAvailableDays(),
                                    p.getAvailableTimes(),
                                    p.getAnswerCycle(),
                                    p.getAvgAnswerCount(),
                                    p.getVersion()
                            )
                    ))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    }


}