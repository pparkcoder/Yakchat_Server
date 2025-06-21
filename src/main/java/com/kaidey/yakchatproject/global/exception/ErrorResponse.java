package com.kaidey.yakchatproject.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final int statusCode;
    private final String error;
    private final String message;
}
