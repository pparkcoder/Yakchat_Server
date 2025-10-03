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
public class ReplyNotifier {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final FcmService fcmService;

    public void notify(Long questionAuthorId, Long questionId, Long replyId, String questionTitle, Long replierId) {
        if (questionAuthorId.equals(replierId)) {
            return;
        }

        List<UserDeviceToken> tokens = userDeviceTokenRepository
                .findByUserIdAndEnabledTrueAndReplyNotifyOptInTrue(questionAuthorId);

        if (tokens.isEmpty()) {
            return;
        }

        String title = "내 질문에 새 답변이 달렸어요";
        String body = "\"" + questionTitle + "\"에 새 답변";
        String deepLink = "/question/" + questionId + "?replyId=" + replyId;
        Map<String, String> data = Map.of(
                "type", "reply",
                "questionId", questionId.toString(),
                "replyId", replyId.toString()
        );

        List<String> tokenStrings = tokens.stream()
                .map(UserDeviceToken::getToken)
                .toList();

        fcmService.sendToTokens(tokenStrings, title, body, deepLink, data);
    }
}