package com.kaidey.yakchatproject.domain.material.request;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MaterialCreateRequest {

	private String title;
	private Long subjectId;

	@Size(max = 10)
	private List<String> urlKey;

}
