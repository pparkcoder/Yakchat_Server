package com.kaidey.yakchatproject.domain.onboarding.service;

import com.kaidey.yakchatproject.domain.onboarding.dto.*;
import com.kaidey.yakchatproject.domain.onboarding.entity.*;
import com.kaidey.yakchatproject.domain.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpertOnboardingService {
    private final ExpertProfileRepository repo;


    @Transactional
    public ExpertOnboardingRes upsert(Long userId, ExpertOnboardingReq req) {
        ExpertProfile p = repo.findById(userId).orElseGet(() -> {
            ExpertProfile np = new ExpertProfile();
            np.setUserId(userId);
            np.setVersion(0);
            return np;
        });


        if (req.strongSubjects().size() > 5)
            throw new IllegalArgumentException("strongSubjects limit exceeded (<=5)");


        p.setStrongSubjects(req.strongSubjects());
        p.setJob(req.job());
        p.setWorkplace(req.workplace());
        p.setAvailableDays(req.availableDays());
        p.setAvailableTimes(req.availableTimes());
        p.setAnswerCycle(req.answerCycle());
        p.setAvgAnswerCount(req.avgAnswerCount());


        p.setVersion(p.getVersion()==null?1:p.getVersion()+1);
        repo.save(p);
        return new ExpertOnboardingRes(userId, true, p.getVersion());
    }


    @Transactional(readOnly = true)
    public Optional<ExpertProfile> get(Long userId) { return repo.findById(userId); }
}