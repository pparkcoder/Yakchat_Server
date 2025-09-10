package com.kaidey.yakchatproject.domain.user.controller;

import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeRequest;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.service.ProfileService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ImageService imageService;

    @Autowired
    public ProfileController(ProfileService profileService, JwtTokenProvider jwtTokenProvider, ImageService imageService) {
        this.profileService = profileService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.imageService = imageService;
    }

    // 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable Long userId) {
        ProfileDto profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // 자기 자신의 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ProfileDto> getMyProfile(@RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        ProfileDto profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // 닉네임 변경
    @PatchMapping("/me/nickname")
    public ResponseEntity<NicknameChangeResponse> changeMyNickname(
            @RequestHeader("Authorization") String token,
            @RequestBody NicknameChangeRequest request) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        NicknameChangeResponse resp = profileService.changeNickname(userId, request.getNickname());
        return ResponseEntity.ok(resp);
    }

    // 닉네임 가용성 체크
    @GetMapping("/nicknames/availability")
    public ResponseEntity<Map<String, Boolean>> checkNicknameAvailability(@RequestParam String nickname) {
        boolean available = profileService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }


    // 프로필 업데이트 (이미지 및 기타 데이터)
//    @PutMapping
//    public ResponseEntity<ProfileDto> updateProfile(
//            @RequestParam(value = "username", required = false) String username,
//            @RequestParam(value = "school", required = false) String school,
//            @RequestParam(value = "grade", required = false) String grade,
//            @RequestParam(value = "age", required = false) Integer age,
//            @RequestParam(value = "images", required = false) List<String> keys,
//            @RequestHeader("Authorization") String token) {
//
//        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
//        ProfileDto profileDto = new ProfileDto();
//        profileDto.setUsername(username);
//        profileDto.setSchool(school);
//        profileDto.setGrade(grade);
//        profileDto.setAge(age);
//
//        if (images != null && !images.isEmpty()) {
//            try {
//                List<Image> uploadedImages = imageService.saveAnswerImages(images, null); // Answer 없이 저장
//                List<ImageDto> imageDtos = new ArrayList<>();
//                for (Image image : uploadedImages) {
//                    ImageDto imageDto = new ImageDto();
////                    imageDto.setOriginalFileName(image.getOriginalFileName());
////                    imageDto.setStoreFileName(image.getStoreFileName());
//                    imageDto.setUrl(image.getUrl());
//                    imageDto.setMime(image.getMime());
//                    imageDtos.add(imageDto);
//                }
//                profileDto.setImages(imageDtos);
//            } catch (IOException e) {
//                return ResponseEntity.status(500).body(null);
//            }
//        }
//
//        ProfileDto updatedProfile = profileService.updateProfile(userId, profileDto);
//        return ResponseEntity.ok(updatedProfile);
//    }
}