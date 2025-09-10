package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record StudentOnboardingRes(
        Long userId,
        boolean completed,
        Long profileVersion
) {}
