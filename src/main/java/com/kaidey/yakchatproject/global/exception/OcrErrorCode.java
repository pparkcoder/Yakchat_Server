package com.kaidey.yakchatproject.global.exception;

import org.springframework.http.HttpStatus;

public enum OcrErrorCode {
    OCR_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "OCR_001", "OCR 서비스에 연결할 수 없습니다"),
    INVALID_DOCUMENT_FORMAT(HttpStatus.BAD_REQUEST, "OCR_002", "지원하지 않는 문서 형식입니다"),
    OCR_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "OCR_003", "문서 인증에 실패했습니다"),
    TEMP_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "OCR_004", "임시 토큰이 만료되었습니다"),
    TEMP_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "OCR_005", "유효하지 않은 임시 토큰입니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "OCR_006", "이메일 인증이 완료되지 않았습니다"),
    INVALID_PHARMACY_QUALIFICATION(HttpStatus.BAD_REQUEST, "OCR_007", "약학 관련 자격이 확인되지 않습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    OcrErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }

    /** ✅ 기존 BusinessException(ErrorCode errorCode)과 호환되는 브릿지 */
    public ErrorCode toErrorCode() {
        return new ErrorCode(this.status, this.code, this.message);
    }
}
