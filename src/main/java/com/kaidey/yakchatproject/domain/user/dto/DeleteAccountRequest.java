package com.kaidey.yakchatproject.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteAccountRequest {
    @NotBlank
    private String password;   // 비밀번호 재확인 (소셜계정이면 생략 가능하도록 검증 로직에서 분기)
    private String reason;
    private boolean immediate; // true면 즉시 완전 삭제, false면 유예(소프트) 삭제
}
