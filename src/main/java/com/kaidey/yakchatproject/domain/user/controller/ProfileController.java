package com.kaidey.yakchatproject.domain.user.controller;

import com.kaidey.yakchatproject.domain.user.dto.ProfileDto;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeRequest;
import com.kaidey.yakchatproject.domain.user.dto.NicknameChangeResponse;
import com.kaidey.yakchatproject.domain.user.service.ProfileService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

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
        NicknameChangeResponse updatedProfile = profileService.updateProfile(userId, request);
//        NicknameChangeResponse resp = profileService.changeNickname(userId, request.getNickname());
        return ResponseEntity.ok(updatedProfile);
    }

    // 닉네임 가용성 체크
    @GetMapping("/nicknames/availability")
    public ResponseEntity<Map<String, Boolean>> checkNicknameAvailability(@RequestParam String nickname) {
        boolean available = profileService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }


//    // 프로필 업데이트 (이미지 및 기타 데이터)
//    @PutMapping
//    public ResponseEntity<ProfileDto> updateProfile(
//            @RequestParam(value="username", required = false) String username,
//            @RequestParam(value="school", required = false) String school,
//            @RequestParam(value="grade", required = false) String grade,
//            @RequestParam(value="keys", required = false) List<String> keys,
//            @RequestHeader("Authorization") String token) {
//
//        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
//        ProfileDto profileDto = new ProfileDto();
//        profileDto.setUsername(username);
//        profileDto.setSchool(school);
//        profileDto.setGrade(grade);
//
//        ProfileDto updatedProfile = profileService.updateProfile(userId, profileDto, keys);
//        return ResponseEntity.ok(updatedProfile);
//    }
}