package com.kaidey.yakchatproject.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_grade_id")
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeType grade = GradeType.NONE; // 기본 등급

    private int questionCount = 0;   // 내가 작성한 질문 수
    private int answerCount = 0;     // 내가 작성한 답변 수

    // 통계용 (등급 산정에는 영향 없음)
    private int acceptedCount = 0;             // 채택된 답변 수(선택)
    private int likeCount = 0;                 // 누적 좋아요(선택)
    private int purchasedMaterialCount = 0;    // 구매 건수(선택)
    private int soldMaterialCount = 0;         // 판매 건수(선택)
}
