package com.kaidey.yakchatproject.domain.user.dto;

import com.kaidey.yakchatproject.domain.user.entity.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {


    private String email;
    private String password;
    private String nickname;   // username 역할
    private String realName;
    private UserType userType;
    private Integer age;

    // 학생용 필드
    private String school;
    private String grade;
    private String department;
    private String studentId;;

    // 전문가용 필드
    private String licenseNumber;
    private LocalDate licenseIssuedDate;

    private Boolean isActive;
    private LocalDateTime createdAt; // 등록 날짜
    private LocalDateTime lastLoginAt; // 마지막 로그인
}
