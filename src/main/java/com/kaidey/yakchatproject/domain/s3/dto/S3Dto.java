package com.kaidey.yakchatproject.domain.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class S3Dto {
    private String key;
    private String preSignedUrl;
}
