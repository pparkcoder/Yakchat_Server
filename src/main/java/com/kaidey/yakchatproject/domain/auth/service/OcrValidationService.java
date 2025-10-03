package com.kaidey.yakchatproject.domain.auth.service;

import java.util.Map;

public interface OcrValidationService {
	boolean isValidStudentCard(String fullText, Map<String, Object> fields);

	boolean isValidProfessionalLicense(String fullText, Map<String, Object> fields);

}
