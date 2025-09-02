package com.kaidey.yakchatproject.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OcrFieldsDto {
    private String name;
    private String studentId;
    private String university;
    private String department;
    private String licenseNumber;
    private String issueDate;


}