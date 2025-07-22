package com.kaidey.yakchatproject.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiControllerExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("errorStatus : {} / errorMessage : {}", errorCode.getStatus(), errorCode.getMessage());
        return handleExceptionInternal(errorCode);
    }

    protected ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus().value(),errorCode.getCode(), errorCode.getMessage()));
    }
}
