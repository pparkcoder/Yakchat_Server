package com.kaidey.yakchatproject.domain.answer.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class UpdateAnswerStepsRequest {
    private Long questionId;
    private List<CreateAnswerStepsRequest.StepPayload> steps;
}