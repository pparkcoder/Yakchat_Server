package com.kaidey.yakchatproject.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDto {
    private Long id;
//    private String originalFileName;  // 사용자가 업로드 한 파일 명
//    private String storeFileName; // 저장소에 저장된 파일 명
//    private String base64Data;
//    private Long questionId; // (선택적) 관련 질문 ID
//    private Long answerId; // (선택적) 관련 답변 ID
//    private Long userId;
    private String url; // 이미지 URL
    private String key; // S3 Key
    private String mime; // MIME 타입
}
