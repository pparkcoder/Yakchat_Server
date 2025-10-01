package com.kaidey.yakchatproject.domain.answer.service;

import com.kaidey.yakchatproject.domain.answer.controller.AnswerController;
import com.kaidey.yakchatproject.domain.answer.dto.AnswerWithStepsDto;
import com.kaidey.yakchatproject.domain.answer.dto.AnswerCardDto;
import com.kaidey.yakchatproject.domain.answer.dto.request.CreateAnswerStepsRequest;
import com.kaidey.yakchatproject.domain.answer.dto.request.UpdateAnswerStepsRequest;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.answer.repository.AnswerRepository;
import com.kaidey.yakchatproject.domain.fcm.event.AnswerAcceptedEvent;
import com.kaidey.yakchatproject.domain.fcm.event.ReplyCreatedEvent;
import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.like.entity.Like;
import com.kaidey.yakchatproject.domain.like.repository.LikeRepository;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.question.repository.QuestionRepository;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;
import com.kaidey.yakchatproject.domain.onboarding.repository.StudentProfileRepository;
import com.kaidey.yakchatproject.global.util.StepsJsonUtils;
import com.kaidey.yakchatproject.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final ImageUtils imageUtils = new ImageUtils();
    private final StudentProfileRepository studentProfileRepository;

    @Autowired
    private ApplicationEventPublisher publisher;
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);


    // 답변 생성
    @Transactional
    public AnswerWithStepsDto createAnswer(CreateAnswerStepsRequest request, Long userId) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        String stepsJson = StepsJsonUtils.toJson(request.getSteps());

        Answer answer = new Answer();
        answer.setContent(stepsJson);            // steps 전체를 JSON으로 저장
        answer.setQuestion(question);
        answer.setUser(user);

        // 모든 step의 keys를 합쳐 Answer 이미지로 저장 (Phase1)
        List<String> allKeys = request.getSteps() == null ? List.of()
                : request.getSteps().stream()
                .filter(s -> s.getKeys() != null && !s.getKeys().isEmpty())
                .flatMap(s -> s.getKeys().stream())
                .distinct()
                .toList();

        if (!allKeys.isEmpty()) {
            List<Image> images = imageService.saveAnswerImages(allKeys, answer);
            answer.setImages(images);
        }

        Answer saved = answerRepository.save(answer);

        publisher.publishEvent(new ReplyCreatedEvent(
                question.getUser().getId(),
                question.getId(),
                saved.getId(),
                question.getTitle(),
                user.getId()
        ));
        userService.updateUserActivity(user, 0, 1);

        return toAnswerWithStepsDto(saved);
    }

    //답변 업데이트
    @Transactional
    public AnswerWithStepsDto updateAnswer(Long answerId, UpdateAnswerStepsRequest request, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));

        if (!answer.getUser().getId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NO_AUTHORITY);
        }

        if (!answer.getQuestion().getId().equals(request.getQuestionId())) {
            Question q = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));
            answer.setQuestion(q);
        }

        String stepsJson = StepsJsonUtils.toJson(request.getSteps());
        answer.update(stepsJson, answer.getQuestion()); // modifiedAt 업데이트 포함

        List<String> allKeys = request.getSteps() == null ? List.of()
                : request.getSteps().stream()
                .filter(s -> s.getKeys() != null && !s.getKeys().isEmpty())
                .flatMap(s -> s.getKeys().stream())
                .distinct()
                .toList();

        if (!allKeys.isEmpty()) {
            List<Image> updateImages = imageService.saveAnswerImages(allKeys, answer);
            answer.updateWithImage(answer.getContent(), answer.getQuestion(), updateImages);
        }

        Answer saved = answerRepository.save(answer);
        return toAnswerWithStepsDto(saved);
    }


    @Transactional(readOnly = true)
    public AnswerWithStepsDto getAnswerById(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));
        return toAnswerWithStepsDto(answer);
    }

    @Transactional(readOnly = true)
    public List<AnswerWithStepsDto> getAllAnswers() {
        return answerRepository.findAllByOrderByIsAcceptedDescCreatedAtDesc().stream()
                .map(this::toAnswerWithStepsDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnswerWithStepsDto> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId).stream()
                .map(this::toAnswerWithStepsDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnswerCardsPage(Long questionId, int page, int size, Long me) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("isAccepted"),
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")));

        Page<Answer> pageData =
                answerRepository.findByQuestionId(questionId, pageable);

        List<AnswerCardDto> items = pageData.getContent().stream().map(a -> {
            var steps = StepsJsonUtils.fromJson(a.getContent());
            var preview = steps.stream().limit(3).toList(); // 프리뷰 3개 고정(원하면 파라미터화)

            AnswerCardDto dto = new AnswerCardDto();
            dto.setId(a.getId());
            dto.setQuestionId(a.getQuestion().getId());
            dto.setAuthor(toAuthor(a.getUser(), me));
            dto.setAccepted(a.getIsAccepted());
            dto.setLikeCount(a.getLikes());
            dto.setCreatedAt(a.getCreatedAt().toString());
            dto.setSteps(preview.stream().map(s -> {
                var st = new AnswerCardDto.Step();
                st.setStepId(s.getId());
                st.setContent(s.getContent());
                st.setImages(List.of()); // Phase1: 비움
                return st;
            }).toList());
            dto.setStepTotal(steps.size());
            dto.setHasMoreSteps(steps.size() > 3);
            return dto;
        }).toList();

        return Map.of(
                "items", items,
                "page", pageData.getNumber(),
                "size", pageData.getSize(),
                "totalPages", pageData.getTotalPages(),
                "totalElements", pageData.getTotalElements(),
                "hasNext", pageData.hasNext()
        );
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

        publisher.publishEvent(new AnswerAcceptedEvent(
                answer.getUser().getId(),      // answerAuthorId
                question.getId(),              // questionId
                answer.getId(),                // answerId
                question.getTitle()            // questionTitle
        ));

        userService.incrementAcceptedCount(answer.getUser(), 1);
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
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
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
        // 내가 작성한 최신 답변 5개 (채택 우선 → 최신순)
        var answers = answerRepository.findTop5ByUserIdOrderByIsAcceptedDescCreatedAtDesc(userId);

        // 질문별 답변 개수 맵
        var answerCount = answers.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId(), Collectors.counting()));

        // 질문 ID 목록
        var questionIds = answers.stream()
                .map(a -> a.getQuestion().getId())
                .distinct()
                .toList();

        // 질문 조회 (기존 방식 유지)
        var questions = questionRepository.findTop5ByIdInOrderByCreatedAtDesc(questionIds).stream()
                .map(this::convertToQuestionWithAnswersDto)
                .collect(Collectors.toList());

        // 개수 매핑
        for (var q : questions) {
            q.setAnswerCount(answerCount.getOrDefault(q.getId(), 0L).intValue());
        }
        return questions;
    }

    @Transactional(readOnly = true)
    public List<AnswerCardDto.Step> getStepsSlice(Long answerId, int offset, int limit) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));

        var steps = StepsJsonUtils.fromJson(answer.getContent()); // 레거시 없음: 바로 파싱
        int off = Math.max(offset, 0);
        int lim = Math.max(limit, 1);

        return steps.stream().skip(off).limit(lim).map(s -> {
            AnswerCardDto.Step st = new AnswerCardDto.Step();
            st.setStepId(s.getId());
            st.setContent(s.getContent());
            st.setImages(List.of()); // Phase1: 비움
            return st;
        }).toList();
    }

    private AnswerCardDto.Author toAuthor(User user, Long meUserId) {
        AnswerCardDto.Author a = new AnswerCardDto.Author();
        a.setId(user.getId());
        a.setSchool(user.getSchool());
        a.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        a.setAvatarUrl(getUserAvatarUrlCached(user.getId()));
        a.setIsMe(meUserId != null && meUserId.equals(user.getId()));

        studentProfileRepository.findByUserId(user.getId())
                .ifPresent(studentProfile -> {
                    a.setGrade(studentProfile.getGrade());
                });
        return a;
    }


    private final Map<Long, String> avatarCache = new ConcurrentHashMap<>();

    private String getUserAvatarUrlCached(Long userId) {
        return avatarCache.computeIfAbsent(userId, this::getUserAvatarUrl);
    }

    private String getUserAvatarUrl(Long userId) {
        // 프로필용 이미지 목록 조회 (ImageType.P)
        List<Image> profileImages = imageService.getProfileImage(userId);
        if (profileImages == null || profileImages.isEmpty()) {
            return null; // 기본 아바타를 프론트에서 보여주면 됨
        }

        var imageDtos = imageUtils.convertToImageDtos(profileImages);
        if (imageDtos == null || imageDtos.isEmpty()) {
            return null;
        }

        return imageDtos.get(0).getUrlKey();
    }

    private AnswerWithStepsDto toAnswerWithStepsDto(Answer answer) {
        AnswerWithStepsDto dto = new AnswerWithStepsDto();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setUserId(answer.getUser().getId());
        dto.setNickname(answer.getUser().getNickname());


        dto.setUserAvatarUrl(getUserAvatarUrlCached(answer.getUser().getId()));

        dto.setAccepted(answer.getIsAccepted());
        dto.setLikeCount(answer.getLikes());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setModifiedAt(answer.getModifiedAt());

        var steps = StepsJsonUtils.fromJson(answer.getContent());
        dto.setSteps(
                steps.stream().map(s -> {
                    AnswerWithStepsDto.StepDto sd = new AnswerWithStepsDto.StepDto();
                    sd.setStepId(s.getId());
                    sd.setContent(s.getContent());
                    sd.setImages(List.of());
                    return sd;
                }).toList()
        );

        return dto;
    }

//    // Answer 엔티티를 AnswerDto로 변환
//    private AnswerDto convertToDto(Answer answer) {
//        AnswerDto answerDto = new AnswerDto();
//        answerDto.setId(answer.getId());
//        answerDto.setContent(answer.getContent());
//        answerDto.setQuestionId(answer.getQuestion().getId());
//        answerDto.setUserId(answer.getUser().getId());
//        answerDto.setUserName(answer.getUser().getUsername());
//        answerDto.setCreatedAt(answer.getCreatedAt());
//        answerDto.setModifiedAt(answer.getModifiedAt());
//        answerDto.setLikeCount(answer.getLikes());
//        answerDto.setAccepted(answer.getIsAccepted());
//        //int totalSteps = answer.getContent().split("\n\n|\r\n\r\n").length;
//        answerDto.setImages(imageUtils.convertToImageDtos(answer.getImages()));
//
//        return answerDto;
//    }

    private QuestionWithAnswersDto convertToQuestionWithAnswersDto(Question question) {
        QuestionWithAnswersDto dto = new QuestionWithAnswersDto();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
//        dto.setIsAnonymous(question.getIsAnonymous());
        dto.setSubjectId(question.getSubject().getId());
        dto.setSubjectName(question.getSubject().getName());
        dto.setUserId(question.getUser().getId());
        dto.setUserNickname(question.getUser().getNickname());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getModifiedAt());
        dto.setLikeCount(question.getLikes());
        dto.setViewCount(question.getViewCount());
        dto.setImages(imageUtils.convertToImageDtos(question.getImages()));
        return dto;
    }
}