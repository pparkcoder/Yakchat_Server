package com.kaidey.yakchatproject.domain.image.service;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.ImageErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.kaidey.yakchatproject.domain.image.repository.ImageRepository;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.dto.ImageDto;

import java.util.List;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.ArrayList;

import java.util.Optional;

@Service
public class ImageService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public ImageDto getImageDtoById(Long id) {
        Optional<Image> optionalImage = imageRepository.findById(id);

        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();

            // ImageDto로 변환
            ImageDto imageDto = new ImageDto();
            imageDto.setId(image.getId());
            imageDto.setOriginalFileName(image.getOriginalFileName());
            imageDto.setStoreFileName(image.getStoreFileName());
            imageDto.setMime(image.getMime());
            imageDto.setUrl("/images/" + image.getStoreFileName());
            return imageDto;
        } else {
            throw new BusinessException(ImageErrorCode.NOT_FOUND_IMAGE);
        }
    }

    // 질문 이미지 등록
    public List<Image> saveQuestionImages(List<MultipartFile> files, Question question) throws IOException {
        List<Image> imageList = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.isEmpty()) {
                continue;
            }

            String fileName = createStoreFileName(file.getOriginalFilename());
            String filePath = uploadDir + "/" + fileName;
            String mimeType = file.getContentType();

            File saveFile = new File(filePath);
            file.transferTo(saveFile);

            Image image = new Image();
            image.setOriginalFileName(file.getOriginalFilename());
            image.setStoreFileName(fileName);
            image.setUrl(filePath);
            image.setMime(mimeType);
            image.setUser(question.getUser());
            image.setQuestion(question);
            image.setStepIndex(i); // 프로필 이미지는 StepIndex를 따로 사용하지 않음

            if (question != null) {
                image.setQuestion(question);
            }

            imageList.add(image);
        }

        return imageRepository.saveAll(imageList);
    }

    // 답변 이미지 저장
    public List<Image> saveAnswerImages(List<MultipartFile> files, Answer answer) throws IOException {
        List<Image> imageList = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.isEmpty()) {
                continue;
            }

            String fileName = createStoreFileName(file.getOriginalFilename());
            String filePath = uploadDir + "/" + fileName;
            String mimeType = file.getContentType();

            File saveFile = new File(filePath);
            file.transferTo(saveFile);

            Image image = new Image();
            image.setOriginalFileName(file.getOriginalFilename());
            image.setStoreFileName(fileName);
            image.setUrl(filePath);
            image.setMime(mimeType);
            image.setUser(answer.getUser());
            image.setAnswer(answer);
            image.setStepIndex(i); // 프로필 이미지는 StepIndex를 따로 사용하지 않음

            if (answer != null) {
                image.setAnswer(answer);
            }

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