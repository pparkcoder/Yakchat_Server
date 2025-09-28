package com.kaidey.yakchatproject.domain.fcm.service.notifier;

import com.kaidey.yakchatproject.domain.fcm.entity.UserDeviceToken;
import com.kaidey.yakchatproject.domain.fcm.repository.UserDeviceTokenRepository;
import com.kaidey.yakchatproject.domain.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerNotifier {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final FcmService fcmService;

    public void notify(Long answerAuthorId, Long questionId, Long answerId, String questionTitle) {
        List<UserDeviceToken> tokens = userDeviceTokenRepository
                .findByUserIdAndEnabledTrueAndNotificationOptInTrue(answerAuthorId);

        if (tokens.isEmpty()) {
            return;
        }

        String title = "내 답변이 채택되었습니다 🎉";
        String body = "\"" + questionTitle + "\"에서 내 답변 채택";
        String deepLink = "/question/" + questionId + "?answerId=" + answerId;
        Map<String, String> data = Map.of(
                "type", "answer_accepted",
                "questionId", questionId.toString(),
                "answerId", answerId.toString()
        );

        List<String> tokenStrings = tokens.stream()
                .map(UserDeviceToken::getToken)
                .toList();

        fcmService.sendToTokens(tokenStrings, title, body, deepLink, data);
    }
}
