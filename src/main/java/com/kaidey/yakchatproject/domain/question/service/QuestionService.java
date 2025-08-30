package com.kaidey.yakchatproject.domain.question.service;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.answer.repository.AnswerRepository;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.like.entity.Like;
import com.kaidey.yakchatproject.domain.like.repository.LikeRepository;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionLikeStatusDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.question.repository.QuestionRepository;
import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import com.kaidey.yakchatproject.domain.subject.repository.SubjectRepository;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.QuestionErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;
    private final ImageUtils imageUtils = new ImageUtils();
    private final ImageService imageService;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, SubjectRepository subjectRepository,
                             UserRepository userRepository, LikeRepository likeRepository, UserService userService,
                             AnswerRepository answerRepository, ImageService imageService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.imageService = imageService;
    }

    // 질문 생성
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto, List<String> keys) {
        Subject subject = subjectRepository.findById(questionDto.getSubjectId())
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_SUBJECT));

        User user = userRepository.findById(questionDto.getUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        Question question = new Question();
        question.setTitle(questionDto.getTitle());
        question.setContent(questionDto.getContent());
        question.setSubject(subject);
        question.setUser(user);


        // 이미지가 있으면 미리 리스트에 추가
        if (keys != null && !keys.isEmpty()) {
            List<Image> imageList = imageService.saveQuestionImages(keys, question);
            question.setImages(imageList);
        }

        Question savedQuestion = questionRepository.save(question);
        userService.updateUserActivity(user, 1, 0);
        return convertToDto(savedQuestion);
    }


    // 특정 질문 조회
    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));
        question.incrementViewCount(); // Increment view count
        questionRepository.save(question); //
        return convertToDto(question);
    }

    // 질문 + 답변 조회
    @Transactional(readOnly = true)
    public QuestionWithAnswersDto getQuestionWithAnswers(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

        // 질문 조회수 증가
        question.incrementViewCount();
        questionRepository.save(question);

        // 질문 DTO 생성
        QuestionWithAnswersDto questionDto = convertToQuestionWithAnswersDto(question);

        questionDto.setQuestionOwner(userId != null && userId.equals(question.getUser().getId()));


        // 해당 질문의 답변 목록 추가
        List<AnswerDto> answerDtos = answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId).stream()
                .map(this::convertAnswerToDto)
                .sorted((a1, a2) -> {
                    if (a1.isAccepted() == a2.isAccepted()) return 0;
                    return a1.isAccepted() ? -1 : 1;
                })
                .collect(Collectors.toList());

        questionDto.setAnswers(answerDtos);
        questionDto.setAnswerCount(answerDtos == null ? 0 : answerDtos.size());
        return questionDto;
    }

    // 모든 질문 조회
    @Transactional(readOnly = true)
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 모든 질문 조회 (오래된 순)
    @Transactional(readOnly = true)
    public List<QuestionDto> getAllQuestionsOldestFirst() {
        return questionRepository.findByOrderByCreatedAtAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // 과목 ID로 질문 조회
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsBySubjectId(Long subjectId) {
        return questionRepository.findBySubjectIdOrderByCreatedAtDesc(subjectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 과목 ID로 질문 조회 (오래된 순)
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsBySubjectIdOldestFirst(Long subjectId) {
        return questionRepository.findBySubjectIdOrderByCreatedAtAsc(subjectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 최신 질문 조회 5개
    @Transactional(readOnly = true)
    public List<QuestionDto> getLatestQuestions() {
        return questionRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 과목 ID로 최신 질문 조회 5개
    @Transactional(readOnly = true)
    public List<QuestionDto> getLatestQuestionsBySubjectId(Long subjectId) {
        return questionRepository.findTop5BySubjectIdOrderByCreatedAtDesc(subjectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 질문 업데이트
    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto, List<String> keys) {
        try {
            // Question 찾기
            Question question = questionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

            // Subject 찾기
            Subject subject = subjectRepository.findById(questionDto.getSubjectId())
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_SUBJECT));

            // 질문 업데이트
            if (keys != null && !keys.isEmpty()) { // 이미지가 추가
                List<Image> updateImages = imageService.saveQuestionImages(keys, question);
                question.updateWithImage(questionDto.getTitle(), questionDto.getContent(), subject, updateImages);
            } else {
                question.update(questionDto.getTitle(), questionDto.getContent(), subject);
            }

            return convertToDto(question);

        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    // 질문 삭제
    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // 질문 좋아요
    @Transactional
    public void likeQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));
        if (likeRepository.findByUserIdAndQuestionId(userId, questionId).isEmpty()) {
            Like like = new Like();
            like.setUser(user);
            like.setQuestion(question);
            likeRepository.save(like);
            question.incrementLikes();
        }
    }

    // 질문 좋아요 취소
    @Transactional
    public void unlikeQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));
        List<Like> likes = likeRepository.findByUserIdAndQuestionId(userId, questionId);
        if (likes.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVAILD_REQEUST);
        }
        likeRepository.deleteAll(likes);
        question.decrementLikes();
    }

    public QuestionLikeStatusDto getQuestionLikeStatus(Long questionId, Long userId) {
        long likeCount = likeRepository.countByQuestionId(questionId);
        boolean isLiked = false; // 요청한 user의 해당 질문에 대한 좋아요 여부

        if (userId != null) {
            isLiked = !likeRepository.findByUserIdAndQuestionId(userId, questionId).isEmpty();
        }

        return new QuestionLikeStatusDto(likeCount, isLiked);
    }

    // 질문 검색
    @Transactional(readOnly = true)
    public List<QuestionDto> searchQuestions(String keyword) {
        if (keyword.length() > 100) {
            throw new BusinessException(QuestionErrorCode.INVAILD_QUESTION_KEYWORD);
        }
        return questionRepository.findByTitleContainingOrContentContaining(keyword, keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 회원 ID로 질문 조회 (최신 순)
    @Transactional(readOnly = true)
    public List<QuestionWithAnswersDto> getQuestionsByUserIdByCreatedAtDesc(Long userId) {

        // 내가 작성한 질문 조회
        List<QuestionWithAnswersDto> questions = questionRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToQuestionWithAnswersDto)
                .collect(Collectors.toList());

        // 질문 ID 추출
        List<Long> questionIds = questions.stream().map(QuestionWithAnswersDto::getId).collect(Collectors.toList());

        // 질문 별 답변 조회
        List<AnswerDto> answers = answerRepository.findByQuestionIdInOrderByCreatedAtDesc(questionIds).stream()
                .map(this::convertAnswerToDto)
                .collect(Collectors.toList());

        // 질문 별 답변 개수 count
        Map<Long, Long> answerCount = answers.stream().collect(Collectors.groupingBy(AnswerDto::getQuestionId, Collectors.counting()));

        // 질문 별 답변 개수 매핑
        for (QuestionWithAnswersDto question : questions) {
            question.setAnswerCount(answerCount.getOrDefault(question.getId(), 0L).intValue());
        }
        return questions;
    }

    // 채택 답변 조회 (최신 순)
    @Transactional(readOnly = true)
    public List<QuestionWithAnswersDto> getQuestionsByUserIdByAcceptedByCreatedAtDesc(Long userId) {

        // 내가 작성한 질문 조회
        List<QuestionWithAnswersDto> questions = questionRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToQuestionWithAnswersDto)
                .collect(Collectors.toList());

        // 질문 ID 추출
        List<Long> questionIds = questions.stream().map(QuestionWithAnswersDto::getId).collect(Collectors.toList());

        // 질문 별 채택된 답변 조회
        List<AnswerDto> answers = answerRepository.findByQuestionIdAndIsAcceptedInOrderByCreatedAtDesc(questionIds, true).stream()
                .map(this::convertAnswerToDto)
                .collect(Collectors.toList());

        // 질문 별 답변 개수 count
        Map<Long, Long> answerCount = answers.stream().collect(Collectors.groupingBy(AnswerDto::getQuestionId, Collectors.counting()));

        // 질문 별 답변 개수 매핑
        Iterator<QuestionWithAnswersDto> iterator = questions.iterator();
        while (iterator.hasNext()) {
            QuestionWithAnswersDto question = iterator.next();
            question.setAnswerCount(answerCount.getOrDefault(question.getId(), 0L).intValue());
            if (question.getAnswerCount() == 0L) {
                iterator.remove();
            }
        }

        return questions;
    }


    // Question 엔티티를 QuestionDto로 변환
    private QuestionDto convertToDto(Question question) {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setId(question.getId());
        questionDto.setTitle(question.getTitle());
        questionDto.setContent(question.getContent());
//        questionDto.setIsAnonymous(question.getIsAnonymous());
        questionDto.setSubjectId(question.getSubject().getId());
        questionDto.setSubjectName(question.getSubject().getName()); // Set subject name
        questionDto.setUserId(question.getUser().getId());
        questionDto.setUserName(question.getUser().getUsername()); // Set username based on anonymity
        questionDto.setCreatedAt(question.getCreatedAt());
        questionDto.setUpdatedAt(question.getModifiedAt());
        questionDto.setLikeCount(question.getLikes());
        questionDto.setViewCount(question.getViewCount());
        questionDto.setImages(imageUtils.convertToImageDtos(question.getImages()));

        return questionDto;
    }

    private AnswerDto convertAnswerToDto(Answer answer) {
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

        // 이미지 추가
        int totalSteps = answer.getContent().split("\n\n|\r\n\r\n").length;
        answerDto.setImages(imageUtils.convertToImageDtos(answer.getImages()));

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