package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeRequest;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;

public interface ProfileService {

	ProfileDto getProfile(Long userId);

	NicknameChangeResponse updateProfile(Long userId, NicknameChangeRequest request);

	boolean isNicknameAvailable(String nickname);

	NicknameChangeResponse changeNickname(Long userId, String newNickname);

}
