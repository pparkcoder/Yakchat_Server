package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.EntityNotFoundException;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.domain.onboarding.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;


import java.time.LocalDateTime;
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ImageUtils imageUtils;
    private final StudentProfileRepository studentProfileRepository;

    @Autowired
    public ProfileService(
            UserRepository userRepository,
            ImageUtils imageUtils,
            StudentProfileRepository studentProfileRepository
    ) {
        this.userRepository = userRepository;
        this.imageUtils = imageUtils;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public ProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        StudentProfile sp = studentProfileRepository.findByUserId(userId).orElse(null);


        return convertToProfileDto(user, sp);
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




//    @Transactional
//    public ProfileDto updateProfile(Long userId, ProfileDto profileDto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (profileDto.getImages() != null && !profileDto.getImages().isEmpty()) {
//            user.getImages().clear();
//            for (ImageDto imageDto : profileDto.getImages()) {
//                Image image = new Image();
//                image.setUrl(imageDto.getUrl());
//                image.setOriginalFileName(imageDto.getOriginalFileName());
//                image.setStoreFileName(imageDto.getStoreFileName());
//                image.setUser(user);
//                user.getImages().add(image);
//            }
//        }
//
//        userRepository.save(user);
//        return convertToProfileDto(user);
//    }

    private ProfileDto convertToProfileDto(User user, StudentProfile sp) {
        ProfileDto dto = new ProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setSchool(user.getSchool());
        dto.setGrade(user.getUserGrade());
        dto.setUserType(user.getUserType());
        dto.setImages(imageUtils.convertToImageDtos(user.getImages()));

        // student_profile 존재할 때만 채우기
        if (sp != null) {
            dto.setStudentGrade(sp.getGrade());
            dto.setAge(sp.getAge());
        } else {
            dto.setStudentGrade(null);
        }
        return dto;
    }
}
