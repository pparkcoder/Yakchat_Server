package com.kaidey.yakchatproject.domain.answer.repository;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId);
    List<Answer> findByQuestionIdAndUserIdOrderByCreatedAtDesc(Long questionId, Long userId);
    boolean existsByQuestionIdAndIsAcceptedTrue(Long questionId);
    List<Answer> findByQuestionIdOrderByIsAcceptedDescCreatedAtAsc(Long questionId);
    List<Answer> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.question.id IN :questionIds")
    List<Answer> findByQuestionIdInOrderByCreatedAtDesc(List<Long> questionIds);

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.question.id IN :questionIds AND a.isAccepted = :isAccepted")
    List<Answer> findByQuestionIdAndIsAcceptedInOrderByCreatedAtDesc(List<Long> questionIds, Boolean isAccepted);
}