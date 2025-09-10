package com.kaidey.yakchatproject.domain.onboarding.dto;

import com.kaidey.yakchatproject.domain.onboarding.entity.StudentCourseEntry;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudyTime;

import java.util.List;

public record StudentProfileRes(
        Long userId,
        String grade,
        Integer age,
        List<String> studyDays,
        List<StudyTime> studyTimes,
        List<String> weakSubjects,
        List<String> strongSubjects,
        List<StudentCourseEntry> courses,
        Long version
) {}