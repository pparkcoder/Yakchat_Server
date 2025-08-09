package com.kaidey.yakchatproject.domain.image.entity;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    private String originalFileName; // 사용자가 업로드 한 파일 명

    private String storeFileName; // 저장소에 저장된 파일 명

    private String preSignedUrlKey;

    @Column(nullable = false)
    private String url;

    private String mime; // MIME 타입

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "question_id", nullable = true)
    private Question question; // 연관된 질문 (nullable)

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "answer_id", nullable = true)
    private Answer answer; // 연관된 답변 (nullable)

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // 연관된 사용자 (nullable)

    private int stepIndex;
}