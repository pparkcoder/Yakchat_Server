package com.kaidey.yakchatproject.domain.subject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kaidey.yakchatproject.domain.subject.dto.SubjectDto;
import com.kaidey.yakchatproject.domain.subject.service.SubjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
	private final SubjectService subjectService;

	@GetMapping("/ui")
	public Map<String, Object> listForUi(@RequestParam(defaultValue = "true") boolean activeOnly) {
		return subjectService.listForUi(activeOnly);
	}

	@GetMapping("/search")
	public List<Map<String, String>> search(@RequestParam String q) {
		return subjectService.search(q);
	}

	@GetMapping("/{code}")
	public SubjectDto getDetail(@PathVariable String code) {
		return subjectService.getByCode(code);
	}
}