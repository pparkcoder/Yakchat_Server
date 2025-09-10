package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;

import java.util.List;

public record ExpertProfileRes(
        Long userId,
        String job,
        String workplace,
        List<String> strongSubjects,
        List<String> availableDays,
        List<StudyTime> availableTimes,
        String answerCycle,
        Integer avgAnswerCount,
        Long version
) {}