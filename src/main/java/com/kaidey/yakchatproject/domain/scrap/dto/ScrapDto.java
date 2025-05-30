package com.kaidey.yakchatproject.domain.scrap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScrapDto {
    private Long id;
    private Long scraperId;
    private Long questionId;
    private Long answerId;
}
