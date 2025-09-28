package com.kaidey.yakchatproject.domain.fcm.listener;

import com.kaidey.yakchatproject.domain.fcm.event.AnswerAcceptedEvent;
import com.kaidey.yakchatproject.domain.fcm.service.notifier.AnswerNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerAcceptedListener {

    private final AnswerNotifier answerNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnswerAccepted(AnswerAcceptedEvent event) {
        try {
            answerNotifier.notify(
                    event.answerAuthorId(),
                    event.questionId(),
                    event.answerId(),
                    event.questionTitle()
            );
        } catch (Exception e) {
            log.error("답변 채택 알림 전송 실패", e);
        }
    }
}