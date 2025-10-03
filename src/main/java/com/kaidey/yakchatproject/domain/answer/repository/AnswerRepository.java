package com.kaidey.yakchatproject.domain.answer.repository;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId);

    boolean existsByQuestionIdAndIsAcceptedTrue(Long questionId);

    @EntityGraph(attributePaths = {"user", "user.images", "question"})
    List<Answer> findTop5ByUserIdOrderByIsAcceptedDescCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.question.id IN :questionIds")
    @EntityGraph(attributePaths = {"user", "user.images", "question"})
    List<Answer> findByQuestionIdInOrderByCreatedAtDesc(List<Long> questionIds);

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.question.id IN :questionIds AND a.isAccepted = :isAccepted")
    List<Answer> findByQuestionIdAndIsAcceptedInOrderByCreatedAtDesc(List<Long> questionIds, Boolean isAccepted);

    @EntityGraph(attributePaths = {"user", "user.images", "question"})
    List<Answer> findAllByOrderByIsAcceptedDescCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "user.images", "question"})
    List<Answer> findByQuestionIdOrderByIsAcceptedDescCreatedAtDesc(Long questionId);

    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);



}