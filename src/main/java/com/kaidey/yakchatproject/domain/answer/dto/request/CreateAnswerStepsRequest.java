package com.kaidey.yakchatproject.domain.answer.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class CreateAnswerStepsRequest {
    private Long questionId;          // "20"
    private List<StepPayload> steps;  // [{id, content, keys}, ...]

    @Getter @Setter
    public static class StepPayload {
        private String id;
        private String content;
        private List<String> keys;
    }
}