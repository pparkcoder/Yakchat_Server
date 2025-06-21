package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class UserErrorCode extends ErrorCode{

    public UserErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final UserErrorCode ALREAD_EXIST_NAME = new UserErrorCode
            (HttpStatus.CONFLICT, "ALREADY_EXIST_NAME", "이미 존재하는 이름입니다.");

    public static final UserErrorCode INVALID_USER_NAME = new UserErrorCode
            (HttpStatus.BAD_REQUEST, "INVALID_USER_NAME", "사용할 수 없는 이름입니다.");

    public static final UserErrorCode INVAILD_PASSWORD = new UserErrorCode
            (HttpStatus.BAD_REQUEST, "INVAILD_PASSWORD", "사용할 수 없는 패스워드입니다.");

    public static final UserErrorCode INVAILD_REFRESH_TOKEN = new UserErrorCode
            (HttpStatus.BAD_REQUEST, "INVAILD_REFRESH_TOKEN", "잘못된 refresh token 입니다.");

    public static final UserErrorCode EXPIRED_TOKEN = new UserErrorCode
            (HttpStatus.FORBIDDEN, "EXPIRED_TOKEN", "만료된 token 입니다.");

    public static final UserErrorCode NOT_FOUND_USER = new UserErrorCode
            (HttpStatus.NOT_FOUND, "NOT_FOUND_USER","존재하지 않는 회원입니다.");

    public static final UserErrorCode NOT_MATCHES_PASSWORD = new UserErrorCode
            (HttpStatus.BAD_REQUEST, "NOT_MATCHES_PASSWORD","비밀번호가 일치하지 않습니다.");
}
