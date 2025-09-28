package com.kaidey.yakchatproject.domain.fcm.service.notifier;

import com.kaidey.yakchatproject.domain.fcm.entity.UserDeviceToken;
import com.kaidey.yakchatproject.domain.fcm.repository.UserDeviceTokenRepository;
import com.kaidey.yakchatproject.domain.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectQuestionNotifier {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final FcmService fcmService;

    // TODO: 관심 과목 팔로우 기능 구현 후 수정 필요
    public void notify(Long questionId, Long subjectId, String subjectName, String questionTitle, Long authorId) {
        // 임시: 모든 사용자에게 알림 (실제로는 해당 과목 관심 사용자만)
        // List<Long> followerIds = userSubjectFollowRepository.findFollowersBySubjectId(subjectId);

        String title = "[" + subjectName + "] 새 질문이 올라왔어요";
        String body = "\"" + questionTitle + "\"을 확인해 보세요";
        String deepLink = "/question/" + questionId;
        Map<String, String> data = Map.of(
                "type", "subject_new",
                "subjectId", subjectId.toString(),
                "questionId", questionId.toString()
        );

        // TODO: 관심 과목 팔로워들에게만 알림 발송하도록 수정
        // 현재는 임시로 비활성화
        log.info("새 질문 알림 (임시 비활성화): {} - {}", subjectName, questionTitle);
    }
}