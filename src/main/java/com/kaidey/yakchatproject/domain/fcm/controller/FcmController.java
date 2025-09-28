package com.kaidey.yakchatproject.domain.fcm.controller;

import com.kaidey.yakchatproject.domain.fcm.dto.RegisterTokenRequest;
import com.kaidey.yakchatproject.domain.fcm.entity.UserDeviceToken;
import com.kaidey.yakchatproject.domain.fcm.repository.UserDeviceTokenRepository;
import com.kaidey.yakchatproject.domain.fcm.service.FcmService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final UserDeviceTokenRepository tokenRepository;
    private final FcmService fcmService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(
            @RequestBody RegisterTokenRequest request,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));

        Optional<UserDeviceToken> existingToken = tokenRepository.findByToken(request.getToken());

        if (existingToken.isPresent()) {
            UserDeviceToken deviceToken = existingToken.get();
            deviceToken.setUserId(userId);
            deviceToken.setPlatform(request.getPlatform());
            deviceToken.setReplyNotifyOptIn(request.getReplyNotifyOptIn());
            deviceToken.setEnabled(true);
            tokenRepository.save(deviceToken);
        } else {
            UserDeviceToken deviceToken = new UserDeviceToken();
            deviceToken.setUserId(userId);
            deviceToken.setToken(request.getToken());
            deviceToken.setPlatform(request.getPlatform());
            deviceToken.setReplyNotifyOptIn(request.getReplyNotifyOptIn());
            tokenRepository.save(deviceToken);
        }

        return ResponseEntity.ok("토큰이 등록되었습니다.");
    }

    @PostMapping("/test")
    public ResponseEntity<String> testNotification(@RequestParam String token) {
        try {
            fcmService.sendToToken(
                    token,
                    "테스트 알림",
                    "FCM 연동 테스트입니다",
                    "/",
                    Map.of("type", "test")
            );
            return ResponseEntity.ok("테스트 알림이 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("알림 전송 실패: " + e.getMessage());
        }
    }
}