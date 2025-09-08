package com.kaidey.yakchatproject.domain.onboarding.entity;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StudentCourseEntry {
    private Integer year;
    private List<String> subjects; // 과목 코드 리스트
    private List<String> customSubjects; // 사용자 자유 입력 과목명
}