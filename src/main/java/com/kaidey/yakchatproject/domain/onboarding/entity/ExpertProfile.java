package com.kaidey.yakchatproject.domain.onboarding.entity;

import com.kaidey.yakchatproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;


@Entity @Table(name = "expert_profile")
@Getter @Setter
public class ExpertProfile {
    @Id @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY) @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<String> strongSubjects;

    @Column(length = 20)
    private String job;

    private String workplace;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<String> availableDays;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<StudyTime> availableTimes;

    @Column(length = 10)
    private String answerCycle;

    private Integer avgAnswerCount;

    private Long version;
}