package com.kaidey.yakchatproject.domain.fcm.listener;

import com.kaidey.yakchatproject.domain.fcm.event.QuestionCreatedEvent;
import com.kaidey.yakchatproject.domain.fcm.service.notifier.SubjectQuestionNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionCreatedListener {

    private final SubjectQuestionNotifier subjectQuestionNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuestionCreated(QuestionCreatedEvent event) {
        try {
            subjectQuestionNotifier.notify(
                    event.questionId(),
                    event.subjectId(),
                    event.subjectName(),
                    event.questionTitle(),
                    event.authorId()
            );
        } catch (Exception e) {
            log.error("질문 생성 알림 전송 실패", e);
        }
    }
}
