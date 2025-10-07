package com.kaidey.yakchatproject.domain.subject.dto;

import com.kaidey.yakchatproject.domain.subject.entity.Subject;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubjectDto {

	private Long id;
	private String code;
	private String name;
	private String categoryName;

	public static SubjectDto from(Subject subject) {
		SubjectDto subjectDo = new SubjectDto();
		subjectDo.id = subject.getId();
		subjectDo.code = subject.getCode();
		subjectDo.name = subject.getName();
		subjectDo.categoryName = subject.getCategory().getName();
		return subjectDo;
	}
}
