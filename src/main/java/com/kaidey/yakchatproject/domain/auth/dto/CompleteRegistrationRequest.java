package com.kaidey.yakchatproject.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CompleteRegistrationRequest {
    @NotEmpty @Size(min = 3, max = 50)
    private String nickname;

    @NotEmpty @Size(min = 8)
    private String password;

    @Email @NotEmpty
    private String email;

    private Boolean agreeToTerms = false;
}