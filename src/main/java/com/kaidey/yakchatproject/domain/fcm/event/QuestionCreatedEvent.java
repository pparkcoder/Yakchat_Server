package com.kaidey.yakchatproject.domain.fcm.event;

public record QuestionCreatedEvent(
        Long questionId,
        Long subjectId,
        String subjectName,
        String questionTitle,
        Long authorId
) {}