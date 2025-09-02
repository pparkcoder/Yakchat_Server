package com.kaidey.yakchatproject.domain.user.dto;

import com.kaidey.yakchatproject.domain.user.entity.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private String realName;
    private String nickname;
    private String email;
    private String password;
    private String school;
    private String grade;
//    private Integer age;
    private UserType userType;
}