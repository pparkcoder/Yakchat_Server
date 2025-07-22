package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class CommonErrorCode extends ErrorCode{

    public CommonErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final CommonErrorCode INVAILD_REQEUST = new CommonErrorCode
            (HttpStatus.BAD_REQUEST, "INVAILD_REQEUST", "잘못된 요청입니다.");

    public static final CommonErrorCode COMMON_ERROR = new CommonErrorCode
            (HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    public static final CommonErrorCode INVALID_EMAIL_CODE = new CommonErrorCode
            (HttpStatus.BAD_REQUEST, "INVALID_EMAIL_CODE", "인증번호가 일치하지 않습니다.");

}
