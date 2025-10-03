package com.kaidey.yakchatproject.domain.email.service;

import com.kaidey.yakchatproject.domain.email.dto.EmailDto;

public interface EmailService {

	void sendEmailCodeWithTempToken(EmailDto emailDto, String tempToken);

	void sendEmailCode(EmailDto emailDto);

	void verifyEmailCode(EmailDto emailDto);
}
