package com.kaidey.yakchatproject.domain.subject.repository;

import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @EntityGraph(attributePaths = "category")
    List<Subject> findByActiveTrueOrderByCategory_SortOrderAscCodeAsc();

    @EntityGraph(attributePaths = "category")
    List<Subject> findByNameContainingIgnoreCaseAndActiveTrue(String q);

    Optional<Subject> findByCode(String code);
}