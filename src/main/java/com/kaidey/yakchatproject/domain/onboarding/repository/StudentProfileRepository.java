package com.kaidey.yakchatproject.domain.onboarding.repository;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);
}