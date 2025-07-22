package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class AnswerErrorCode extends ErrorCode{
    public AnswerErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final AnswerErrorCode NOT_FOUND_ANSWER = new AnswerErrorCode
            (HttpStatus.NOT_FOUND, "NOT_FOUND_ANSWER", "답변이 존재하지 않습니다.");

    public static final AnswerErrorCode NOT_ALLOWED_ACCEPT_ANSWER = new AnswerErrorCode
            (HttpStatus.BAD_REQUEST, "NOT_ALLOWED_ACCEPT_ANSWER", "질문 작성자만 답변을 채택할 수 있습니다.");

    public static final AnswerErrorCode ALREADY_ACCEPT_ANSWER = new AnswerErrorCode
            (HttpStatus.BAD_REQUEST, "ALREADY_ACCEPT_ANSWER", "이미 채택된 답변입니다.");
}
