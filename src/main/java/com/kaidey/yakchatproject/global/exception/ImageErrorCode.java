package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public class ImageErrorCode extends ErrorCode{
    public ImageErrorCode(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static final ImageErrorCode NOT_FOUND_IMAGE = new ImageErrorCode
            (HttpStatus.NOT_FOUND, "NOT_FOUND_IMAGE", "이미지를 찾을 수 없습니다.");
}
