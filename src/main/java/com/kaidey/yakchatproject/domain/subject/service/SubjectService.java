package com.kaidey.yakchatproject.domain.subject.service;

import java.util.List;
import java.util.Map;

import com.kaidey.yakchatproject.domain.subject.entity.Subject;

public interface SubjectService {

	Map<String, Object> listForUi(boolean activeOnly);

	List<Map<String, String>> search(String q);

	Subject getByCode(String code);
}

