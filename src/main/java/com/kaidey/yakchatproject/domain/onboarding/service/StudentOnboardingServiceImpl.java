package com.kaidey.yakchatproject.domain.onboarding.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kaidey.yakchatproject.domain.onboarding.dto.StudentCourseDto;
import com.kaidey.yakchatproject.domain.onboarding.dto.StudentOnboardingReq;
import com.kaidey.yakchatproject.domain.onboarding.dto.StudentOnboardingRes;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentCourseEntry;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;
import com.kaidey.yakchatproject.domain.onboarding.repository.StudentProfileRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentOnboardingServiceImpl implements StudentOnboardingService {
	private final StudentProfileRepository studentProfileRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public StudentOnboardingRes upsert(Long userId, StudentOnboardingReq req) {
		var userRef = userRepository.getReferenceById(userId);

		StudentProfile p = studentProfileRepository.findById(userId).orElseGet(() -> {
			StudentProfile np = new StudentProfile();
			np.setUser(userRef);
			return np;
		});

		// null-safe 리스트들
		var weak = req.weakSubjects() == null ? List.<String>of() : req.weakSubjects();
		var strong = req.strongSubjects() == null ? List.<String>of() : req.strongSubjects();
		var days = req.studyDays() == null ? List.<String>of() : req.studyDays();
		var times = req.studyTimes() == null ? List.<StudyTime>of() : req.studyTimes();
		var coursesReq = req.courses() == null ? List.<StudentCourseDto>of() : req.courses();

		if (weak.size() > 5)
			throw new IllegalArgumentException("weakSubjects limit exceeded (<=5)");
		if (strong.size() > 5)
			throw new IllegalArgumentException("strongSubjects limit exceeded (<=5)");

		// 값 매핑
		p.setGrade(req.grade());
		p.setAge(req.age());
		p.setStudyDays(days);
		p.setStudyTimes(times);
		p.setWeakSubjects(weak);
		p.setStrongSubjects(strong);
		p.setCourses(
			coursesReq.stream()
				.map(c -> new StudentCourseEntry(c.year(), c.subjects(), c.customSubjects()))
				.toList()
		);

		studentProfileRepository.saveAndFlush(p);

		return new StudentOnboardingRes(userId, true, p.getVersion());

	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StudentProfile> get(Long userId) {
		return studentProfileRepository.findById(userId);
	}
}
