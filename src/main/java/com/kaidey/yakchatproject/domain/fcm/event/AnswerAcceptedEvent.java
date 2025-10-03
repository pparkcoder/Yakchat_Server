package com.kaidey.yakchatproject.domain.fcm.event;

public record AnswerAcceptedEvent(
        Long answerAuthorId,
        Long questionId,
        Long answerId,
        String questionTitle
) {}