package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record StudentOnboardingReq(
        @NotBlank String grade,                                  // 1~6학년/휴학/졸업
        @NotNull @Min(15) @Max(80) Integer age,                  // 나이
        @NotNull @Size(min = 1, max = 7) List<@NotBlank String> studyDays,   // ["월","수","금"]
        @NotNull @Size(min = 1, max = 4) List<StudyTime> studyTimes,         // [오전, 저녁]
        @NotNull @Size(max = 5) List<@NotBlank String> weakSubjects,         // 어려워하는 과목 코드
        @NotNull @Size(max = 5) List<@NotBlank String> strongSubjects,       // 자신있는 과목 코드
        @NotNull @Size(max = 6) List<@Valid StudentCourseDto> courses        // 학년별 수강 과목
) {}