package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class UserErrorCode extends ErrorCode{

    public UserErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final UserErrorCode NICKNAME_TAKEN = new UserErrorCode(
            HttpStatus.CONFLICT, "NICKNAME_TAKEN", "이미 사용 중인 닉네임입니다."
    );

    public static final UserErrorCode NICKNAME_INVALID = new UserErrorCode(
            HttpStatus.BAD_REQUEST, "NICKNAME_INVALID", "유효하지 않은 닉네임입니다."
    );

    public static final UserErrorCode NICKNAME_SAME_AS_BEFORE = new UserErrorCode(
            HttpStatus.BAD_REQUEST, "NICKNAME_SAME_AS_BEFORE", "현재 닉네임과 동일합니다."
    );
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

    public static final UserErrorCode ALREADY_EXIST_EMAIL = new UserErrorCode(
            HttpStatus.CONFLICT, "ALREADY_EXIST_EMAIL", "이미 사용 중인 이메일입니다."
    );
    public static final UserErrorCode EMAIL_NOT_VERIFIED = new UserErrorCode(
            HttpStatus.PRECONDITION_FAILED, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."
    );

    public static final UserErrorCode INVALID_EMAIL = new UserErrorCode(
            HttpStatus.BAD_REQUEST, "INVALID_EMAIL", "유효하지 않은 이메일입니다."
    );

    public static final UserErrorCode DELETED_USER = new UserErrorCode(
            HttpStatus.FORBIDDEN, "DELETED_USER", "탈퇴한 회원입니다."
    );

}
