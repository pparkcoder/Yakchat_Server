package com.kaidey.yakchatproject.domain.answer.service;

import com.kaidey.yakchatproject.domain.answer.controller.AnswerController;
import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.answer.repository.AnswerRepository;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.repository.ImageRepository;
import com.kaidey.yakchatproject.domain.like.entity.Like;
import com.kaidey.yakchatproject.domain.like.repository.LikeRepository;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.question.repository.QuestionRepository;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.global.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final ImageUtils imageUtils = new ImageUtils();
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);


    @Autowired
    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         UserRepository userRepository,
                         LikeRepository likeRepository,
                         ImageRepository imageRepository,
                         ImageService imageService,
                         UserService userService) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
        this.userService = userService;
    }

    // 답변 생성
    @Transactional
    public AnswerDto createAnswer(AnswerDto answerDto, List<MultipartFile> images) {
        try {
            // Question과 User 찾기
            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

            User user = userRepository.findById(answerDto.getUserId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

            // Answer 객체 생성
            Answer answer = new Answer();
            answer.setContent(answerDto.getContent());
            answer.setQuestion(question);
            answer.setUser(user);

            // 이미지가 있으면 미리 리스트에 추가
            List<Image> imageList = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                imageList = imageService.saveImages(images, answer);
                answer.setImages(imageList);
            }

            // Answer 저장
            Answer savedAnswer = answerRepository.save(answer);

            log.info("Saved answer ID: {}, Images count: {}", savedAnswer.getId(), savedAnswer.getImages() != null ? savedAnswer.getImages().size() : "null");

            return convertToDto(savedAnswer);
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    // 특정 답변 조회
    @Transactional(readOnly = true)
    public AnswerDto getAnswerById(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));
        return convertToDto(answer);
    }

    // 특정 질문과 사용자에 대한 답변 조회
    @Transactional(readOnly = true)
    public List<AnswerDto> getAnswersByQuestionIdAndUserId(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        List<Answer> answers = answerRepository.findByQuestionIdAndUserIdOrderByCreatedAtDesc(questionId, userId);

        if (answers.isEmpty()) {
            return List.of(); // Return an empty list if no answers are found
        }

        return answers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // 모든 답변 조회
    @Transactional(readOnly = true)
    public List<AnswerDto> getAllAnswers() {
        return answerRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    //답변 업데이트
    @Transactional
    public AnswerDto updateAnswer(Long id, AnswerDto answerDto,  List<MultipartFile> images, List<Long> deleteImageIds) {
        // Answer 찾기
        try {
            Answer answer = answerRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));

            answer.setContent(answerDto.getContent());

            // Question 찾기 및 설정
            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));
            answer.setQuestion(question);

            // 삭제할 이미지가 있다면 제거
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                List<Image> imagesToDelete = imageRepository.findAllById(deleteImageIds);
                for (Image image : imagesToDelete) {
                    answer.getImages().remove(image);
                    imageRepository.delete(image); // DB에서 삭제
                }
            }

            // 새로운 이미지 추가
            if (images != null && !images.isEmpty()) {
                List<Image> savedImages = imageService.saveImages(images, answer);
                answer.getImages().addAll(savedImages);
            }

            answer.updateModifiedAt();

            // DTO 변환 후 반환
            return convertToDto(answerRepository.save(answer));
        } catch (IOException e){
            throw new BusinessException(CommonErrorCode.INVAILD_REQEUST);
        }
    }

    @Transactional(readOnly = true)
    public List<AnswerDto> getAnswersByQuestionId(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId);
        return answers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(Long id) {
        answerRepository.deleteById(id);
    }

    @Transactional
    public void acceptAnswer(Long answerId, User user) {
        // 답변 찾기
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));

        // 질문 찾기
        Question question = answer.getQuestion();

        // 질문 작성자만 답변을 채택할 수 있도록 제한
        if (!question.getUser().getId().equals(user.getId())) {
            throw new BusinessException(AnswerErrorCode.NOT_ALLOWED_ACCEPT_ANSWER);
        }

        // 이미 채택된 답변이 있는지 확인
        if (answerRepository.existsByQuestionIdAndIsAcceptedTrue(question.getId())) {
            throw new BusinessException(AnswerErrorCode.ALREADY_ACCEPT_ANSWER);
        }

        // 답변 채택
        answer.setIsAccepted(true);
        answerRepository.save(answer);

        // 답변 작성자의 활동 점수 업데이트 (예: 채택된 답변 개수 증가)
        userService.updateUserActivity(answer.getUser(), 0, 1, 0, 0, 0);
    }

    @Transactional
    public void likeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));
        if (likeRepository.findByUserIdAndAnswerId(userId, answerId).isEmpty()) {
            Like like = new Like();
            like.setUser(user); // Assuming User entity has a constructor with ID
            like.setAnswer(answer);
            likeRepository.save(like);
            answer.incrementLikes();
        }
    }

    @Transactional
    public void unlikeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));
        Like like = likeRepository.findByUserIdAndAnswerId(userId, answerId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVAILD_REQEUST));
        likeRepository.delete(like);
        answer.decrementLikes();

    }

    @Transactional(readOnly = true)
    public long getAnswerLikeCount(Long answerId) {
        return likeRepository.countByAnswerId(answerId);
    }

    // 회원 ID로 답변 조회 (최신 순)
    @Transactional(readOnly = true)
    public List<QuestionWithAnswersDto> getAnswersByUserIdByCreatedAtDesc(Long userId) {

        // 내가 작성한 답변 조회
        List<AnswerDto> answers = answerRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 질문 ID 별 답변 개수 count
        Map<Long, Long> answerCount = answers.stream().collect(Collectors.groupingBy(AnswerDto::getQuestionId, Collectors.counting()));

        // 질문 ID 추출
        List<Long> questionIds = answers.stream().distinct().map(AnswerDto::getQuestionId).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());

        // 질문 조회
        List<QuestionWithAnswersDto> questions = questionRepository.findTop5ByIdInOrderByCreatedAtDesc(questionIds).stream()
                .map(this::convertToQuestionWithAnswersDto)
                .collect(Collectors.toList());

        // 질문 별 답변 개수 매핑
        for (QuestionWithAnswersDto question : questions) {
            question.setAnswerCount(answerCount.getOrDefault(question.getId(),0L).intValue());
        }

        return questions;
    }

    // Answer 엔티티를 AnswerDto로 변환
    private AnswerDto convertToDto(Answer answer) {
        AnswerDto answerDto = new AnswerDto();
        answerDto.setId(answer.getId());
        answerDto.setContent(answer.getContent());
        answerDto.setQuestionId(answer.getQuestion().getId());
        answerDto.setUserId(answer.getUser().getId());
        answerDto.setUserName(answer.getUser().getUsername());
        answerDto.setCreatedAt(answer.getCreatedAt());
        answerDto.setModifiedAt(answer.getModifiedAt());
        answerDto.setLikeCount(answer.getLikes());
        answerDto.setAccepted(answer.getIsAccepted());
        int totalSteps = answer.getContent().split("\n\n|\r\n\r\n").length;
        answerDto.setImages(imageUtils.convertToImageMap(answer.getImages(), totalSteps));

        return answerDto;
    }

    private QuestionWithAnswersDto convertToQuestionWithAnswersDto(Question question) {
        QuestionWithAnswersDto dto = new QuestionWithAnswersDto();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
//        dto.setIsAnonymous(question.getIsAnonymous());
        dto.setSubjectId(question.getSubject().getId());
        dto.setSubjectName(question.getSubject().getName());
        dto.setUserId(question.getUser().getId());
        dto.setUserName(question.getUser().getUsername());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getModifiedAt());
        dto.setLikeCount(question.getLikes());
        dto.setViewCount(question.getViewCount());
        dto.setImages(imageUtils.convertToImageDtos(question.getImages()));
        return dto;
    }
}