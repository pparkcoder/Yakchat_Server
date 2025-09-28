package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeRequest;
import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ImageUtils imageUtils;
    private final ImageService imageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public ProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        List<Image> findProfileImage = imageService.getProfileImage(userId);

        return convertToProfileDto(user, findProfileImage);
    }

    @Transactional
    public NicknameChangeResponse updateProfile(Long userId, NicknameChangeRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));
            if(request.getKeys() != null && !request.getKeys().isEmpty()) {
                List<Image> images = imageService.saveProfileImages(request.getKeys(), user);
                user.updateWithImage(request.getNickname(), images);
            } else{
                user.update(request.getNickname());
            }
            return new NicknameChangeResponse(
                    user.getNickname(),
                    user.getLastNicknameChangedAt().toString()
            );
        } catch (Exception e){
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }


    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.isBlank()) return false;
        // 대소문자 무시 중복 확인
        return !userRepository.existsByNicknameIgnoreCase(nickname);
    }


    @Transactional
    public NicknameChangeResponse changeNickname(Long userId, String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new BusinessException(UserErrorCode.NICKNAME_INVALID);
        }
        if (userRepository.existsByNicknameIgnoreCase(newNickname)) {
            throw new BusinessException(UserErrorCode.NICKNAME_TAKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        // 동일 닉네임이면 에러로 처리(원하면 그냥 OK 반환으로 바꿔도 됨)
        if (user.getNickname() != null
                && user.getNickname().equalsIgnoreCase(newNickname)) {
            throw new BusinessException(UserErrorCode.NICKNAME_SAME_AS_BEFORE);
        }

        user.setNickname(newNickname);
        user.setLastNicknameChangedAt(LocalDateTime.now());
        userRepository.save(user);

        return new NicknameChangeResponse(
                user.getNickname(),
                user.getLastNicknameChangedAt().toString()
        );
    }

    private ProfileDto convertToProfileDto(User user, List<Image> images) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setId(user.getId());
        profileDto.setUsername(user.getUsername());
        profileDto.setNickname(user.getNickname());
        profileDto.setEmail(user.getEmail());
        profileDto.setSchool(user.getSchool());
        profileDto.setGrade(user.getUserGrade());
        profileDto.setUserType(user.getUserType());
        profileDto.setImages(imageUtils.convertToImageDtos(images));
        return profileDto;
    }
}
