package com.kaidey.yakchatproject.domain.answer.dto;

import com.kaidey.yakchatproject.domain.image.dto.ImageDto;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class AnswerWithStepsDto {
    private Long id;
    private Long questionId;
    private Long userId;
    private String nickname;
    private String userAvatarUrl;
    private Boolean accepted;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // steps 응답
    private List<StepDto> steps;

    @Getter @Setter
    public static class StepDto {
        private String stepId;        // 프론트가 넘긴 구분자 id
        private String content;
        private List<ImageDto> images; // (Phase 1에서는 Answer 단위로만 저장하면 비울 수도 있음)
    }
}