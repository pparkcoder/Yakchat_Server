package com.kaidey.yakchatproject.domain.subject.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kaidey.yakchatproject.domain.subject.dto.SubjectDto;
import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import com.kaidey.yakchatproject.domain.subject.repository.SubjectRepository;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectService {
	private final SubjectRepository subjectRepository;

	// UI용 과목 목록 조회
	public Map<String, Object> listForUi(boolean activeOnly) {
		List<Subject> list = activeOnly
			? subjectRepository.findByActiveTrueOrderByCategory_SortOrderAscCodeAsc()
			: subjectRepository.findAll();

		Map<String, List<Subject>> grouped = list.stream().collect(
			Collectors.groupingBy(s -> s.getCategory().getCode(), LinkedHashMap::new, Collectors.toList())
		);

		List<Map<String, Object>> sections = new ArrayList<>();
		for (Map.Entry<String, List<Subject>> e : grouped.entrySet()) {
			String sectionCode = e.getKey();
			List<Subject> subs = e.getValue();
			if (subs.isEmpty())
				continue;
			String title = subs.get(0).getCategory().getName();

			List<Map<String, String>> items = subs.stream().map(s -> Map.of(
				"code", s.getCode(),
				"label", s.getName()
			)).collect(Collectors.toList());

			sections.add(Map.of(
				"sectionCode", sectionCode,
				"sectionTitle", title,
				"items", items
			));
		}
		return Map.of("sections", sections);
	}

	// 검색어로 과목 검색 (활성화된 과목만)
	public List<Map<String, String>> search(String q) {
		return subjectRepository.findByNameContainingIgnoreCaseAndActiveTrue(q).stream()
			.map(s -> Map.of(
				"code", s.getCode(),
				"label", s.getName(),
				"category", s.getCategory().getName()
			)).toList();
	}

	// 코드로 과목 조회
	public SubjectDto getByCode(String code) {
		Subject subject = subjectRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
		return SubjectDto.from(subject);
	}
}

