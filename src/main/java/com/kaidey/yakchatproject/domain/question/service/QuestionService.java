package com.kaidey.yakchatproject.domain.question.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionLikeStatusDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;

public interface QuestionService {

	QuestionDto createQuestion(QuestionDto questionDto, List<String> keys);

	QuestionDto getQuestionById(Long id);

	QuestionWithAnswersDto getQuestionWithAnswers(Long questionId, Long userId);

	List<QuestionDto> getAllQuestions();

	List<QuestionDto> getAllQuestionsOldestFirst();

	List<QuestionDto> getQuestionsBySubjectId(Long subjectId);

	List<QuestionDto> getQuestionsBySubjectIdOldestFirst(Long subjectId);

	List<QuestionDto> getLatestQuestions();

	List<QuestionDto> getLatestQuestionsBySubjectId(Long subjectId);

	QuestionDto updateQuestion(Long id, QuestionDto questionDto, List<String> keys);

	void deleteQuestion(Long id);

	void likeQuestion(Long questionId, Long userId);

	void unlikeQuestion(Long questionId, Long userId);

	QuestionLikeStatusDto getQuestionLikeStatus(Long questionId, Long userId);

	List<QuestionDto> searchQuestions(String keyword);

	List<QuestionWithAnswersDto> getQuestionsByUserIdByCreatedAtDesc(Long userId);

	List<QuestionWithAnswersDto> getQuestionsByUserIdByAcceptedByCreatedAtDesc(Long userId);

}