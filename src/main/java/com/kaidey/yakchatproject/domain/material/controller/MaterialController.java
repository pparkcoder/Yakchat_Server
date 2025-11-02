package com.kaidey.yakchatproject.domain.material.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kaidey.yakchatproject.domain.material.request.MaterialCreateRequest;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;
import com.kaidey.yakchatproject.domain.material.service.MaterialService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;

import jakarta.validation.Valid;
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
		@Valid @RequestBody MaterialCreateRequest request,
		@RequestHeader("Authorization") String token) {

		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		return ResponseEntity.ok(materialService.createMaterial(request, userId));
	}

	// 학습자료 조회
	@GetMapping("/{id}")
	public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long id) {
		return ResponseEntity.ok(materialService.getMaterialById(id));
	}

	// 회원 ID로 학습자료 조회
	@GetMapping
	public ResponseEntity<MaterialResponse> getMaterialByUserId(@RequestParam("userId") Long userId) {
		return ResponseEntity.ok(materialService.getMaterialByUserId(userId));
	}

	// 학습자료 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Boolean> deleteMaterialById(@PathVariable Long id) {
		return ResponseEntity.ok(materialService.deleteMaterialById(id));
	}
}
