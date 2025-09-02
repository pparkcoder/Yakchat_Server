package com.kaidey.yakchatproject.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OcrStatusResponse {
    private boolean verified;
    private String documentType;
    private OcrFieldsDto fields;
    private Long expiresIn;
    private String extractedName;
}