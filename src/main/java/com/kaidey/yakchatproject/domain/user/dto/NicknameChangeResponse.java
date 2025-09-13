package com.kaidey.yakchatproject.domain.user.dto;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NicknameChangeResponse {
    private String nickname;
    private String changedAt; // ISO-8601 문자열
}