package com.kaidey.yakchatproject.domain.image.service;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.kaidey.yakchatproject.domain.image.repository.ImageRepository;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.dto.ImageDto;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ImageRepository imageRepository;

    public ImageDto getImageDtoById(Long id) {
        Optional<Image> optionalImage = imageRepository.findById(id);

        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();

            // ImageDto로 변환
            ImageDto imageDto = new ImageDto();
            imageDto.setId(image.getId());
            imageDto.setMime(image.getMime());
            //imageDto.setUrl("/images/" + image.getStoreFileName());
            imageDto.setKey("");
            return imageDto;
        } else {
            throw new BusinessException(ImageErrorCode.NOT_FOUND_IMAGE);
        }
    }

    // 질문 이미지 등록
    public List<Image> saveQuestionImages(List<String> keys, Question question) {
        List<Image> imageList = new ArrayList<>();
        for (String key : keys) {
            Image image = new Image();
            image.setUser(question.getUser());
            image.setQuestion(question);
            image.setUrlKey(key);
            //image.setStepIndex(i); // 프로필 이미지는 StepIndex를 따로 사용하지 않음
            imageList.add(image);
        }
        return imageRepository.saveAll(imageList);
    }

    // 답변 이미지 저장
    public List<Image> saveAnswerImages(List<String> keys, Answer answer) {
        List<Image> imageList = new ArrayList<>();
        for (String key : keys) {
            Image image = new Image();
            image.setUser(answer.getUser());
            image.setAnswer(answer);
            image.setUrlKey(key);
            imageList.add(image);
        }

        return imageRepository.saveAll(imageList);
    }

    // 저장소에 저장될 파일 명 생성
    private String createStoreFileName(String originalFileName) {
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // 확장자 추출
    private String extractExt(String originalFileName) {
        int index = originalFileName.lastIndexOf(".");
        return originalFileName.substring(index + 1);
    }
}