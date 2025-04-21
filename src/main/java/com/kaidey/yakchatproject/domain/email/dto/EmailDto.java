package com.kaidey.yakchatproject.domain.email.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailDto {

    @NotEmpty
    private String email;
    private String code;
}
