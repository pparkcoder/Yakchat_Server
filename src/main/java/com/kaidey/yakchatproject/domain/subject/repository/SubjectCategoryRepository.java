package com.kaidey.yakchatproject.domain.subject.repository;

import com.kaidey.yakchatproject.domain.subject.entity.SubjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectCategoryRepository extends JpaRepository<SubjectCategory, Long> {
    Optional<SubjectCategory> findByCode(String code);
}