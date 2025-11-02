package com.kaidey.yakchatproject.domain.material.response;

import java.util.List;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import com.kaidey.yakchatproject.domain.material.entity.Material;

import lombok.Getter;

@Getter
public class MaterialResponse {

	private Long id;
	private Long userId;
	private String title;
	private List<ImageDto> images;

	public static MaterialResponse of(Material material, List<ImageDto> images) {
		MaterialResponse response = new MaterialResponse();
		response.id = material.getId();
		response.userId = material.getUser().getId();
		response.title = material.getTitle();
		response.images = images;
		return response;
	}
}
