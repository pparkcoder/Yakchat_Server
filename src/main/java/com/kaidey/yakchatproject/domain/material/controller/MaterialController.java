package com.kaidey.yakchatproject.domain.material.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kaidey.yakchatproject.domain.material.request.MaterialCreateRequest;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;
import com.kaidey.yakchatproject.domain.material.service.MaterialService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

	private final JwtTokenProvider jwtTokenProvider;
	private final MaterialService materialService;

	// 학습자료 생성
	@PostMapping
	public ResponseEntity<MaterialResponse> createMaterial(
		@RequestBody MaterialCreateRequest request,
		@RequestHeader("Authorization") String token) {

		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		return ResponseEntity.ok(materialService.createMaterial(request, userId));
	}
}
