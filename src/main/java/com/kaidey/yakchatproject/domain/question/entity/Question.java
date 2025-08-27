package com.kaidey.yakchatproject.domain.question.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
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
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

//    @Column(nullable = false)
//    private Boolean isAnonymous; // Add this field

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime modifiedAt;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Answer> answers = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    private int likes = 0;

    @Column(nullable = false)
    private int viewCount = 0;

    public void incrementLikes() {
        this.likes++;
    }
    public void decrementLikes() {
        if(--this.likes < 0){
            this.likes = 0;
        }
    }
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, Subject subject){
        this.title = title;
        this.content = content;
        this.subject = subject;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateWithImage(String title, String content, Subject subject, List<Image> images){
        this.title = title;
        this.content = content;
        this.subject = subject;
        this.modifiedAt = LocalDateTime.now();

        for (Image image : this.images) {
            image.setQuestion(null);
        }
        this.images.clear();
        for (Image image : images) {
            this.images.add(image);
            image.setQuestion(this);
        }
    }
}
