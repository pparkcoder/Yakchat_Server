package com.kaidey.yakchatproject.domain.fcm.event;

public record ReplyCreatedEvent(
        Long questionAuthorId,
        Long questionId,
        Long replyId,
        String questionTitle,
        Long replierId
) {}
