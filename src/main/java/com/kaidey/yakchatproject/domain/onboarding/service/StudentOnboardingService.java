package com.kaidey.yakchatproject.domain.onboarding.service;

import com.kaidey.yakchatproject.domain.onboarding.dto.*;
import com.kaidey.yakchatproject.domain.onboarding.entity.*;
import com.kaidey.yakchatproject.domain.onboarding.repository.*;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service

@RequiredArgsConstructor
public class StudentOnboardingService {
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepo;

    @Transactional
    public StudentOnboardingRes upsert(Long userId, StudentOnboardingReq req) {
        var userRef = userRepo.getReferenceById(userId);

        StudentProfile p = studentProfileRepository.findById(userId).orElseGet(() -> {
            StudentProfile np = new StudentProfile();
            np.setUser(userRef);
            return np;
        });

        if (req.weakSubjects() != null && req.weakSubjects().size() > 5)
            throw new IllegalArgumentException("weakSubjects limit exceeded (<=5)");
        if (req.strongSubjects() != null && req.strongSubjects().size() > 5)
            throw new IllegalArgumentException("strongSubjects limit exceeded (<=5)");

        // 값 매핑
        p.setGrade(req.grade());
        p.setAge(req.age());
        p.setStudyDays(req.studyDays());
        p.setStudyTimes(req.studyTimes());
        p.setWeakSubjects(req.weakSubjects());
        p.setStrongSubjects(req.strongSubjects());
        p.setCourses(
                req.courses().stream()
                        .map(c -> new StudentCourseEntry(c.year(), c.subjects(), c.customSubjects()))
                        .toList()
        );

        studentProfileRepository.save(p); // 신규면 persist, 기존이면 update

        return new StudentOnboardingRes(userId, true, p.getVersion());
    }

    @Transactional(readOnly = true)
    public Optional<StudentProfile> get(Long userId) { return studentProfileRepository.findById(userId); }
}
