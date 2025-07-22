package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class QuestionErrorCode extends ErrorCode{

    public QuestionErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final QuestionErrorCode NOT_FOUND_SUBJECT = new QuestionErrorCode
            (HttpStatus.NOT_FOUND, "NOT_FOUND_SUBJECT", "존재하지 않는 과목입니다.");

    public static final QuestionErrorCode NOT_FOUND_QUESTION = new QuestionErrorCode
            (HttpStatus.NOT_FOUND, "NOT_FOUND_QUESTION", "존재하지 않는 질문입니다.");

    public static final QuestionErrorCode INVAILD_QUESTION_KEYWORD = new QuestionErrorCode
            (HttpStatus.BAD_REQUEST, "INVAILD_QUESTION_KEYWORD", "질문 검색 키워드는 100글자 이하여야 합니다.");
}
