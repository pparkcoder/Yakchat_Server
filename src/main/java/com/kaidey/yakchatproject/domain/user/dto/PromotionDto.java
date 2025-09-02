package com.kaidey.yakchatproject.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromotionDto {
    private String currentGrade;
    private String nextGrade;
    private int progress;
    private int target;
    private double rate;
}
