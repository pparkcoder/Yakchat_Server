package com.kaidey.yakchatproject.domain.image.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.user.entity.User;

public interface ImageService {

	ImageDto getImageDtoById(Long id);

	List<Image> saveQuestionImages(List<String> keys, Question question);

	List<Image> saveAnswerImages(List<String> keys, Answer answer);

	List<Image> saveProfileImages(List<String> keys, User user);

	List<Image> getProfileImage(Long userId);
}