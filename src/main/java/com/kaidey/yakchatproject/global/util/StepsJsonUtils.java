package com.kaidey.yakchatproject.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaidey.yakchatproject.domain.answer.dto.request.CreateAnswerStepsRequest.StepPayload;
import java.util.List;

public class StepsJsonUtils {
    private static final ObjectMapper om = new ObjectMapper();

    public static String toJson(List<StepPayload> steps) {
        try { return om.writeValueAsString(steps); }
        catch (Exception e) { throw new RuntimeException("steps serialize error", e); }
    }

    public static List<StepPayload> fromJson(String json) {
        try { return om.readValue(json, new TypeReference<>(){}); }
        catch (Exception e) { throw new RuntimeException("steps deserialize error", e); }
    }
}