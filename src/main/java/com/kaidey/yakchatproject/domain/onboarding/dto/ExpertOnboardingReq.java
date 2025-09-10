package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ExpertOnboardingReq(
        @NotNull @Size(min=1, max=5) List<@NotBlank String> strongSubjects,  // 자신있는 과목 코드
        @NotBlank String job,                                                // 직업 (예: 개국약사)
        String workplace,                                                    // 근무지
        @NotNull @Size(min=1, max=7) List<@NotBlank String> availableDays,   // 가능 요일
        @NotNull @Size(min=1, max=4) List<StudyTime> availableTimes,         // 가능 시간대
        @NotBlank String answerCycle,                                        // 답변 주기
        @NotNull @Min(0) @Max(1000) Integer avgAnswerCount                   // 평균 답변 가능 개수
) {}