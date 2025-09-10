package com.kaidey.yakchatproject.domain.onboarding.entity;

import com.kaidey.yakchatproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
@Entity
@Table(name = "student_profile")
@Getter @Setter
public class StudentProfile implements org.springframework.data.domain.Persistable<Long> {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY) @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 10)
    private String grade;

    private Integer age;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<String> studyDays;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<StudyTime> studyTimes;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<String> weakSubjects;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json")
    private List<String> strongSubjects;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<StudentCourseEntry> courses;

    @Version
    @Column(name = "version")
    private Long version;

    // 신규/기존 판별 안정화 (merge 오판 방지)
    @Transient
    private boolean _isNew = true;

    @Override public Long getId() { return userId; }
    @Override public boolean isNew() { return _isNew || version == null; }
    @PostLoad @PostPersist void markNotNew() { this._isNew = false; }
    public void markNew() { this._isNew = true; }
}
