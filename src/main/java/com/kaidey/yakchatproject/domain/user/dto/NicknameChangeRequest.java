package com.kaidey.yakchatproject.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NicknameChangeRequest {
    @NotBlank
    private String nickname;
    private List<String> keys;
}