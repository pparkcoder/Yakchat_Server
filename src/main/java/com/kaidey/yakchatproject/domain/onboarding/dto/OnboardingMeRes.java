package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.user.entity.UserType;

public record OnboardingMeRes(
        UserType userType,
        StudentProfileRes student,       // STUDENT인 경우에만 채움 (아니면 null)
        ExpertProfileRes professional    // PROFESSIONAL인 경우에만 채움 (아니면 null)
) {}