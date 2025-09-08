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
public class StudentOnboardingService {
    private final StudentProfileRepository repo;


    @Transactional
    public StudentOnboardingRes upsert(Long userId, StudentOnboardingReq req) {
        StudentProfile p = repo.findById(userId).orElseGet(() -> {
            StudentProfile np = new StudentProfile();
            np.setUserId(userId);
            np.setVersion(0);
            return np;
        });


        if (req.weakSubjects().size() > 5 || req.strongSubjects().size() > 5)
            throw new IllegalArgumentException("subjects limit exceeded (<=5)");


        p.setGrade(req.grade());
        p.setAge(req.age());
        p.setStudyDays(req.studyDays());
        p.setStudyTimes(req.studyTimes());
        p.setWeakSubjects(req.weakSubjects());
        p.setStrongSubjects(req.strongSubjects());
        p.setCourses(req.courses().stream().map(c -> new StudentCourseEntry(c.year(), c.subjects(), c.customSubjects())).toList());


        p.setVersion(p.getVersion()==null?1:p.getVersion()+1);
        repo.save(p);
        return new StudentOnboardingRes(userId, true, p.getVersion());
    }


    @Transactional(readOnly = true)
    public Optional<StudentProfile> get(Long userId) { return repo.findById(userId); }
}