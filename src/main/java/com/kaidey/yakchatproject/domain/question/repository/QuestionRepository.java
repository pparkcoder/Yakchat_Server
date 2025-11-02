package com.kaidey.yakchatproject.domain.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kaidey.yakchatproject.domain.question.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	List<Question> findByTitleContainingOrContentContaining(String title, String content);

	List<Question> findByOrderByCreatedAtDesc();

	List<Question> findBySubjectIdOrderByCreatedAtDesc(Long subjectId);

	List<Question> findByOrderByCreatedAtAsc();

	List<Question> findBySubjectIdOrderByCreatedAtAsc(Long subjectId);

	List<Question> findTop5ByOrderByCreatedAtDesc();

	List<Question> findTop5BySubjectIdOrderByCreatedAtDesc(Long subjectId);

	List<Question> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

	List<Question> findTop5ByIdInOrderByCreatedAtDesc(List<Long> questionIds);
}