package com.kaidey.yakchatproject.domain.material.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.material.request.MaterialCreateRequest;
import com.kaidey.yakchatproject.domain.material.request.MaterialUpdateRequest;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;

public interface MaterialService {

	MaterialResponse createMaterial(MaterialCreateRequest request, Long userId);

	MaterialResponse updateMaterial(MaterialUpdateRequest request, Long userId);

	MaterialResponse getMaterialById(Long id);

	List<MaterialResponse> getMaterialByUserId(Long userId);

	Boolean deleteMaterialById(Long id);
}
