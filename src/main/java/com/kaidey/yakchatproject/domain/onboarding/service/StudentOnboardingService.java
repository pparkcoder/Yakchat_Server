package com.kaidey.yakchatproject.domain.onboarding.service;

import java.util.Optional;

import com.kaidey.yakchatproject.domain.onboarding.dto.StudentOnboardingReq;
import com.kaidey.yakchatproject.domain.onboarding.dto.StudentOnboardingRes;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;

public interface StudentOnboardingService {

	StudentOnboardingRes upsert(Long userId, StudentOnboardingReq req);

	Optional<StudentProfile> get(Long userId);
}
