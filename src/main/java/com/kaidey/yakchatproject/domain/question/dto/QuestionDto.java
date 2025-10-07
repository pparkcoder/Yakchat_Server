package com.kaidey.yakchatproject.domain.question.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDto {
	private Long id;
	private String title;
	private String content;
	private Long subjectId;
	private String subjectName;
	private Long userId;
	private String nickname;
	private List<ImageDto> images;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Integer likeCount;
	private Integer viewCount;
	private Integer answerCount;
}
