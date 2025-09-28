package com.kaidey.yakchatproject.domain.fcm.listener;

import com.kaidey.yakchatproject.domain.fcm.event.ReplyCreatedEvent;
import com.kaidey.yakchatproject.domain.fcm.service.notifier.ReplyNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyCreatedListener {

    private final ReplyNotifier replyNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReplyCreated(ReplyCreatedEvent event) {
        try {
            replyNotifier.notify(
                    event.questionAuthorId(),
                    event.questionId(),
                    event.replyId(),
                    event.questionTitle(),
                    event.replierId()
            );
        } catch (Exception e) {
            log.error("답변 생성 알림 전송 실패", e);
        }
    }
}