package com.kaidey.yakchatproject.domain.archive.controller;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.service.AnswerService;
import com.kaidey.yakchatproject.domain.archive.service.ArchiveService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.question.service.QuestionService;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArchiveService archiveService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final ImageUtils imageUtils = new ImageUtils();

    @Autowired
    public ArchiveController(UserService userService, JwtTokenProvider jwtTokenProvider, ArchiveService archiveService
                , QuestionService questionService, AnswerService answerService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.archiveService = archiveService;
        this.questionService = questionService;
        this.answerService = answerService;
    }

    // 내 질문 조회(최신순 5개)
    @GetMapping("/my-questions")
    public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllQuestions(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<QuestionWithAnswersDto> questions = questionService.getQuestionsByUserIdByCreatedAtDesc(userId);
        return ResponseEntity.ok(questions);
    }

    // 내 답변 조회(최신순 5개)
    @GetMapping("/my-answers")
    public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllAnswers(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<QuestionWithAnswersDto> answers = answerService.getAnswersByUserIdByCreatedAtDesc(userId);
        return ResponseEntity.ok(answers);
    }

    // 채택 답변이 있는 질문 조회(최신순 5개)
    @GetMapping("/my-questions/accepted")
    public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllQuestionsByAccepted(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<QuestionWithAnswersDto> questions = questionService.getQuestionsByUserIdByAcceptedByCreatedAtDesc(userId);
        return ResponseEntity.ok(questions);
    }

    // 질문 스크랩
    @PostMapping("/question-scraps")
    public ResponseEntity<ScrapDto> creatQuestionScrap(
            @RequestParam("questionId") Long questionId,
            @RequestHeader("Authorization") String token){

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        ScrapDto scrapDto = new ScrapDto();
        scrapDto.setScraperId(userId);
        scrapDto.setQuestionId(questionId);
        ScrapDto newScrap = archiveService.creatQuestionScrap(scrapDto);
        return ResponseEntity.ok(newScrap);
    }

    // 답변 스크랩
    @PostMapping("/answer-scraps")
    public ResponseEntity<ScrapDto> creatAnswerScrap(
            @RequestParam("questionId") Long questionId,
            @RequestParam("answerId") Long answerId,
            @RequestHeader("Authorization") String token){

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        ScrapDto scrapDto = new ScrapDto();
        scrapDto.setScraperId(userId);
        scrapDto.setAnswerId(answerId);
        scrapDto.setQuestionId(questionId);
        ScrapDto newScrap = archiveService.creatAnswerScrap(scrapDto);
        return ResponseEntity.ok(newScrap);
    }

    // 질문 스크랩 보기
    @GetMapping("/question-scraps")
    public ResponseEntity<List<QuestionDto>> getQuestionScraps(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<QuestionDto> scraps = archiveService.getQuestionScraps(userId);
        return ResponseEntity.ok(scraps);
    }

    // 답변 스크랩 보기
    @GetMapping("/answer-scraps")
    public ResponseEntity<List<AnswerDto>> getAnswerScrap(@RequestHeader("Authorization") String token){
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<AnswerDto> scraps = archiveService.getAnswerScraps(userId);
        return ResponseEntity.ok(scraps);
    }
}