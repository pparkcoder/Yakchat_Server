package com.kaidey.yakchatproject.domain.fcm.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    public void sendToToken(String token, String title, String body, String deepLink, Map<String, String> extra) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Map<String, String> data = Map.of(
                    "deep_link", deepLink != null ? deepLink : "/",
                    "type", extra.getOrDefault("type", ""),
                    "questionId", extra.getOrDefault("questionId", ""),
                    "replyId", extra.getOrDefault("replyId", ""),
                    "answerId", extra.getOrDefault("answerId", ""),
                    "subjectId", extra.getOrDefault("subjectId", "")
            );

            Message message = Message.builder()
                    .setNotification(notification)
                    .putAllData(data)
                    .setToken(token)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 성공: {}", response);
        } catch (Exception e) {
            log.error("FCM 발송 실패: {}", e.getMessage());
            throw new RuntimeException("FCM 발송 실패", e);
        }
    }

    public void sendToTokens(List<String> tokens, String title, String body, String deepLink, Map<String, String> extra) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Map<String, String> data = Map.of(
                    "deep_link", deepLink != null ? deepLink : "/",
                    "type", extra.getOrDefault("type", ""),
                    "questionId", extra.getOrDefault("questionId", ""),
                    "replyId", extra.getOrDefault("replyId", ""),
                    "answerId", extra.getOrDefault("answerId", ""),
                    "subjectId", extra.getOrDefault("subjectId", "")
            );

            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .putAllData(data)
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("FCM 멀티캐스트 발송 - 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.error("FCM 멀티캐스트 발송 실패: {}", e.getMessage());
            throw new RuntimeException("FCM 멀티캐스트 발송 실패", e);
        }
    }
}