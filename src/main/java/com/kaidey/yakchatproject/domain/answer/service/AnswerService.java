package com.kaidey.yakchatproject.domain.answer.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.user.entity.User;

public interface AnswerService {

	AnswerDto createAnswer(AnswerDto answerDto, List<String> keys);

	AnswerDto getAnswerById(Long id);

	List<AnswerDto> getAnswersByQuestionIdAndUserId(Long questionId, Long userId);

	List<AnswerDto> getAllAnswers();

	AnswerDto updateAnswer(Long id, AnswerDto answerDto, List<String> keys);

	List<AnswerDto> getAnswersByQuestionId(Long questionId);

	void deleteAnswer(Long id);

	void acceptAnswer(Long answerId, User user);

	void likeAnswer(Long answerId, Long userId);

	void unlikeAnswer(Long answerId, Long userId);

	long getAnswerLikeCount(Long answerId);

	List<QuestionWithAnswersDto> getAnswersByUserIdByCreatedAtDesc(Long userId);

}