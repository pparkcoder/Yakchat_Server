package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;
import com.kaidey.yakchatproject.domain.onboarding.repository.StudentProfileRepository;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeRequest;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ImageUtils imageUtils;
    private final ImageService imageService;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        // 프로필 이미지 (dev-temp 유지)
        List<Image> profileImages = imageService.getProfileImage(userId);

        // 학생 프로필은 선택적: 없으면 null 반환 → DTO에 안전하게 매핑
        StudentProfile sp = studentProfileRepository.findByUserId(userId).orElse(null);

        return convertToProfileDto(user, profileImages, sp);
    }

    @Transactional
    public NicknameChangeResponse updateProfile(Long userId, NicknameChangeRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

            if (request.getKeys() != null && !request.getKeys().isEmpty()) {
                List<Image> images = imageService.saveProfileImages(request.getKeys(), user);
                user.updateWithImage(request.getNickname(), images);
            } else {
                user.update(request.getNickname());
            }

            return new NicknameChangeResponse(
                    user.getNickname(),
                    user.getLastNicknameChangedAt() != null
                            ? user.getLastNicknameChangedAt().toString()
                            : LocalDateTime.now().toString()
            );
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.isBlank()) return false;
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

        if (user.getNickname() != null && user.getNickname().equalsIgnoreCase(newNickname)) {
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

    private ProfileDto convertToProfileDto(User user, List<Image> images, StudentProfile sp) {
        ProfileDto dto = new ProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setSchool(user.getSchool());
        dto.setGrade(user.getUserGrade());
        dto.setUserType(user.getUserType());
        dto.setImages(imageUtils.convertToImageDtos(images));

        // StudentProfile이 있을 때만 안전하게 채움 (NPE 회피)
        if (sp != null) {
            dto.setStudentGrade(sp.getGrade());
            dto.setAge(sp.getAge());
        } else {
            dto.setStudentGrade(null);
            dto.setAge(null);
        }
        return dto;
    }
}
