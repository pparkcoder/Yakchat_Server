package com.kaidey.yakchatproject.domain.material.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MaterialUpdateRequest {

	@NotNull
	private Long id;

	@NotNull
	private String title;

	@NotNull
	private Long subjectId;

	@Size(max = 10)
	private List<String> urlKey;

}
