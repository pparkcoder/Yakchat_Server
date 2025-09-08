package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record StudentCourseDto(
        @NotNull @Min(1) @Max(6) Integer year,
        @NotNull @Size(max = 100) List<@NotBlank String> subjects,
        @NotNull @Size(max = 100) List<@NotBlank String> customSubjects
) {}