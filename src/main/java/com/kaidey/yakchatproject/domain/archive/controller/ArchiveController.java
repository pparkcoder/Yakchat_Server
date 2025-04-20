package com.kaidey.yakchatproject.domain.archive.controller;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.archive.service.ArchiveService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArchiveService archiveService;
    private final ImageUtils imageUtils = new ImageUtils();

    @Autowired
    public ArchiveController(UserService userService, JwtTokenProvider jwtTokenProvider, ArchiveService archiveService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.archiveService = archiveService;
    }

    // 자신이 적은 모든 질문 조회
    @GetMapping("/my-questions")
    public ResponseEntity<List<QuestionDto>> getMyAllQuestions(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        User findUser = userService.getUserById(userId);
        List<QuestionDto> questions = findUser.getQuestions().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questions);
    }

    // 자신이 적은 모든 답변 조회
    @GetMapping("/my-answers")
    public ResponseEntity<List<AnswerDto>> getMyAllAnswers(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        User findUser = userService.getUserById(userId);
        List<AnswerDto> answers = findUser.getAnswers().stream()
                .map(this::convertToAnswerDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(answers);
    }

    // 스크랩
    @PostMapping("/{id}/scrap")
    public ResponseEntity<ScrapDto> scrap(@PathVariable("id") Long questionId
            , @RequestHeader("Authorization") String token){

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        ScrapDto scrapDto = new ScrapDto();
        scrapDto.setScraperId(userId);
        scrapDto.setQuestionId(questionId);
        ScrapDto newScrap = archiveService.createScrap(scrapDto);
        return ResponseEntity.ok(newScrap);
    }

    // 스크랩 보기
    @GetMapping("/scraps")
    public ResponseEntity<List<QuestionDto>> getScraps(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<QuestionDto> scraps = archiveService.getScrapsByUserId(userId);
        return ResponseEntity.ok(scraps);
    }

    private AnswerDto convertToAnswerDto(Answer answer){
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

    private QuestionDto convertToQuestionDto(Question question){
        QuestionDto questionDto = new QuestionDto();
        questionDto.setId(question.getId());
        questionDto.setTitle(question.getTitle());
        questionDto.setContent(question.getContent());
        questionDto.setSubjectId(question.getSubject().getId());
        questionDto.setSubjectName(question.getSubject().getName());
        questionDto.setUserId(question.getUser().getId());
        questionDto.setUserName(question.getUser().getUsername());
        questionDto.setCreatedAt(question.getCreatedAt());
        questionDto.setUpdatedAt(question.getModifiedAt());
        questionDto.setLikeCount(question.getLikes());
        questionDto.setViewCount(question.getViewCount());
        questionDto.setImages(imageUtils.convertToImageDtos(question.getImages()));

        return questionDto;
    }
}