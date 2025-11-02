package com.kaidey.yakchatproject.domain.material.service;

import com.kaidey.yakchatproject.domain.material.request.MaterialCreateRequest;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;

public interface MaterialService {

	MaterialResponse createMaterial(MaterialCreateRequest request, Long userId);

	MaterialResponse getMaterialById(Long id);

	MaterialResponse getMaterialByUserId(Long userId);

	Boolean deleteMaterialById(Long id);
}
