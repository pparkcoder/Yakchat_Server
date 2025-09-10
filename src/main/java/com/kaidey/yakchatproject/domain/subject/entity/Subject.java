package com.kaidey.yakchatproject.domain.subject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.util.List;


@Entity
@Table(name = "subject")
@Getter @Setter
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")   // ★ DB의 PK 이름과 일치시킴
    private Long id;

    @Column(length = 32, unique = true, nullable = false)
    private String code;           // UK와 일치

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // FK와 일치
    private SubjectCategory category;

    @Column(nullable = false)
    private boolean active = true; // MySQL bit(1) ←→ boolean 매핑 OK

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> tags;
}
