package com.kaidey.yakchatproject.domain.answer.dto;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import lombok.Getter;import lombok.Setter;
import java.util.List;

@Getter @Setter
public class AnswerCardDto {
    private Long id;                // answerId
    private Long questionId;

    private Author author;

    private Boolean accepted;
    private Integer likeCount;
    private String createdAt;

    private List<Step> steps;
    private Integer stepTotal;
    private Boolean hasMoreSteps;

    @Getter @Setter
    public static class Author {
        private Long id;            // userId
        private String nickname;        // userName
        private String school;        // 선택
        private String grade;        // 선택
        private String avatarUrl;   // 선택
        private Boolean isMe;       // 선택
    }

    @Getter @Setter
    public static class Step {
        private String stepId;
        private String content;
        private List<ImageDto> images;
    }
}