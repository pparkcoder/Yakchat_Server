package com.kaidey.yakchatproject.domain.onboarding.service;

import java.util.Optional;

import com.kaidey.yakchatproject.domain.onboarding.dto.ExpertOnboardingReq;
import com.kaidey.yakchatproject.domain.onboarding.dto.ExpertOnboardingRes;
import com.kaidey.yakchatproject.domain.onboarding.entity.ExpertProfile;

public interface ExpertOnboardingService {

	ExpertOnboardingRes upsert(Long userId, ExpertOnboardingReq req);

	Optional<ExpertProfile> get(Long userId);
}