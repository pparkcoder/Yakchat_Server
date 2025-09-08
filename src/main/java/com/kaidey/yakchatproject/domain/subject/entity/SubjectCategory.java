package com.kaidey.yakchatproject.domain.subject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "subject_category")
@Getter @Setter
public class SubjectCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 16, unique = true, nullable = false)
    private String code; // BIO, IND, CLN ...


    @Column(length = 50, nullable = false)
    private String name; // 생명약학 등


    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}