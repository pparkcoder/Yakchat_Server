package com.kaidey.yakchatproject.domain.onboarding.service;

import com.kaidey.yakchatproject.domain.onboarding.dto.*;
import com.kaidey.yakchatproject.domain.onboarding.entity.*;
import com.kaidey.yakchatproject.domain.onboarding.repository.*;
import com.kaidey.yakchatproject.domain.user.entity.*;
import com.kaidey.yakchatproject.domain.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertOnboardingService {
    private final ExpertProfileRepository expertProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpertOnboardingRes upsert(Long userId, ExpertOnboardingReq req) {
        var userRef = userRepository.getReferenceById(userId);

        ExpertProfile p = expertProfileRepository.findById(userId).orElseGet(() -> {
            ExpertProfile np = new ExpertProfile();
            np.setUser(userRef);
            return np;
        });

        var strong = req.strongSubjects() == null ? List.<String>of() : req.strongSubjects();
        var days   = req.availableDays()   == null ? List.<String>of() : req.availableDays();
        var times  = req.availableTimes()  == null ? List.<StudyTime>of() : req.availableTimes();

        if (strong.size() > 5) throw new IllegalArgumentException("strongSubjects limit exceeded (<=5)");
        if (days.size() > 7)   throw new IllegalArgumentException("availableDays limit exceeded (<=7)");
        if (times.size() > 4)  throw new IllegalArgumentException("availableTimes limit exceeded (<=4)");

        p.setJob(req.job());
        p.setWorkplace(req.workplace());
        p.setStrongSubjects(strong);
        p.setAvailableDays(days);
        p.setAvailableTimes(times);
        p.setAnswerCycle(req.answerCycle());
        p.setAvgAnswerCount(req.avgAnswerCount());

        // JPA @Version 사용 시 수동 증가 불필요
        expertProfileRepository.saveAndFlush(p);

        return new ExpertOnboardingRes(userId, true, p.getVersion());
    }

    @Transactional(readOnly = true)
    public Optional<ExpertProfile> get(Long userId) { return expertProfileRepository.findById(userId); }
}