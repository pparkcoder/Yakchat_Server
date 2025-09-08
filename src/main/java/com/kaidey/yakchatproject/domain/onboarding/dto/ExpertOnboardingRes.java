package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ExpertOnboardingRes(
        Long userId,
        boolean completed,
        Integer profileVersion
) {}