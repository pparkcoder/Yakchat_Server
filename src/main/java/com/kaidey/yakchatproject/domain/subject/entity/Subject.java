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
    private Long id;


    @Column(length = 32, unique = true, nullable = false)
    private String code; // 예: IND-PHARM


    @Column(length = 100, nullable = false)
    private String name; // 라벨


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private SubjectCategory category;


    @Column(nullable = false)
    private boolean active = true;


    // 태그(선택). 검색이나 필터에 활용 가능. 필요 없으면 제거 가능
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> tags;
}