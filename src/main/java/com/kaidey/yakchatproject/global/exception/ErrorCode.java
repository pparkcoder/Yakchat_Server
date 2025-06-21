package com.kaidey.yakchatproject.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ErrorCode {

    private final HttpStatus status;
    private final String code;
    private final String message;
}
