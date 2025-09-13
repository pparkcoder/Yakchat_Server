package com.kaidey.yakchatproject.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class NicknameChangeResponse {
    private String nickname;
    private String changedAt; // ISO-8601 문자열
}