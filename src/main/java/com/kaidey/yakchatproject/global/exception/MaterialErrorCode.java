package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class MaterialErrorCode extends ErrorCode {

	public MaterialErrorCode(HttpStatus status, String code, String message) {
		super(status, code, message);
	}

	public static final MaterialErrorCode NOT_FOUND_MATERIAL = new MaterialErrorCode
		(HttpStatus.NOT_FOUND, "NOT_FOUND_SUBJECT", "존재하지 않는 학습자료입니다.");
}
