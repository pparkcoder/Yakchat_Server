package com.kaidey.yakchatproject.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OcrVerificationResponse {
    private boolean success;
    private String tempToken;
    private String documentType;
    private OcrFieldsDto fields;
    private String message;
}