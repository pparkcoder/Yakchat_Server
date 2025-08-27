package com.kaidey.yakchatproject.domain.answer.entity;

import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @Column(nullable = false)
    private String content; // 답변 내용


    @Column(nullable = false)
    private Boolean isAccepted = false; // 채택 여부

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 연관된 질문

    @ManyToOne(fetch = FetchType.LAZY) // 작성자와의 관계
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 답변 작성자

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 등록 날짜

    private LocalDateTime modifiedAt; // 수정일

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>(); // 답변에 관련된 이미지들

    private int likes = 0; // 좋아요 수

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if(--this.likes < 0){
            this.likes = 0;
        }
    }

    public void update(String content, Question question){
        this.content = content;
        this.question = question;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateWithImage(String content, Question question, List<Image> images){
        this.content = content;
        this.question = question;
        this.modifiedAt = LocalDateTime.now();

        for (Image image : this.images) {
            image.setAnswer(null);
        }
        this.images.clear();
        for (Image image : images) {
            this.images.add(image);
            image.setAnswer(this);
        }
    }
}