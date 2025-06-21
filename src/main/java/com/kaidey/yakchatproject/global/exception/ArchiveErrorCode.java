package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class ArchiveErrorCode extends ErrorCode{

    public ArchiveErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final ArchiveErrorCode ALREADY_SCRAP = new ArchiveErrorCode
            (HttpStatus.BAD_REQUEST, "ALREADY_SCRAP", "이미 스크랩 되었습니다.");
}
