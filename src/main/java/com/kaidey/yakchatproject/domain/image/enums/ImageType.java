package com.kaidey.yakchatproject.domain.image.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageType {
	ANSWER("ANSWER_IMAGE", "답변이미지"),
	QUESTION("QUESTION_IMAGE", "질문이미지"),
	PROFILE("PROFILE_IMAGE", "프로필이미지"),
	MATERIAL("MATERIAL_IMAGE", "학습자료이미지");

	private final String id;
	private final String name;

}
