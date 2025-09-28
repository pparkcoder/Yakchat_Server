package com.kaidey.yakchatproject.domain.user.dto;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import com.kaidey.yakchatproject.domain.user.entity.UserGrade;
import com.kaidey.yakchatproject.domain.user.entity.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfileDto {
    private Long id;
    private String username;
    private Integer age;
    private String school;
    private UserGrade grade;
    private UserType userType;
    private String studentGrade;


//    private String ProfileImage;
//    private String ProfileImageUrl;
    private List<ImageDto> images;

}