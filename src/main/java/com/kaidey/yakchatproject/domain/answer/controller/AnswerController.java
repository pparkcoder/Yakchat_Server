package com.kaidey.yakchatproject.domain.answer.controller;


import com.kaidey.yakchatproject.domain.answer.dto.AnswerWithStepsDto;
import com.kaidey.yakchatproject.domain.answer.dto.request.CreateAnswerStepsRequest;
import com.kaidey.yakchatproject.domain.answer.dto.request.UpdateAnswerStepsRequest;
import com.kaidey.yakchatproject.domain.answer.dto.AnswerCardDto;
import com.kaidey.yakchatproject.domain.answer.service.AnswerService;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {
    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    private final AnswerService answerService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    // 답변 생성
    @PostMapping
    public ResponseEntity<AnswerWithStepsDto> createAnswer(
            @RequestBody CreateAnswerStepsRequest request,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        return ResponseEntity.ok(answerService.createAnswer(request, userId));
    }

//    @GetMapping("/cards")
//    public ResponseEntity<List<AnswerCardDto>> getAnswerCards(
//            @RequestParam Long questionId,
//            @RequestParam(defaultValue = "3") int previewSteps,
//            @RequestHeader("Authorization") String token
//    ) {
//        Long me = jwtTokenProvider.getUserIdFromToken(token.substring(7));
//        return ResponseEntity.ok(answerService.getAnswerCards(questionId, previewSteps, me));
//    }

    //STEP 더보기 (지연로딩)
    @GetMapping("/{answerId}/steps")
    public ResponseEntity<List<AnswerCardDto.Step>> getMoreSteps(
            @PathVariable Long answerId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(answerService.getStepsSlice(answerId, offset, limit));
    }

    @GetMapping("/cards")
    public ResponseEntity<Map<String, Object>> getAnswerCardsPage(
            @RequestParam Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token
    ) {
        Long me = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        var result = answerService.getAnswerCardsPage(questionId, page, size, me);
        return ResponseEntity.ok(result);
    }



    @GetMapping("/{id}")
    public ResponseEntity<AnswerWithStepsDto> getAnswerById(@PathVariable Long id) {
        return ResponseEntity.ok(answerService.getAnswerById(id));
    }

    // 모든 답변 조회
    @GetMapping
    public ResponseEntity<List<AnswerWithStepsDto>> getAllAnswers() {
        return ResponseEntity.ok(answerService.getAllAnswers());
    }

    // 특정 질문에 해당하는 답변 조회
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerWithStepsDto>> getAnswersByQuestionId(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId));
    }

    // 답변 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<AnswerWithStepsDto> updateAnswer(
            @PathVariable Long id,
            @RequestBody UpdateAnswerStepsRequest request,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        return ResponseEntity.ok(answerService.updateAnswer(id, request, userId));
    }


    // 답변 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    // 답변 채택
    @PostMapping("/{answerId}/accept")
    public ResponseEntity<String> acceptAnswer(
            @PathVariable Long answerId,
            @RequestHeader("Authorization") String token) {

        // JWT 토큰에서 유저 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        User user = userService.getUserById(userId);

        answerService.acceptAnswer(answerId, user);
        return ResponseEntity.ok("답변이 채택되었습니다.");
    }


    // 답변 좋아요 수 조회
    @GetMapping("/{id}/likeCount")
    public ResponseEntity<Long> getAnswerLikeCount(@PathVariable Long id) {
        long likeCount = answerService.getAnswerLikeCount(id);
        return ResponseEntity.ok(likeCount);
    }

    // 답변 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeAnswer(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        answerService.likeAnswer(id, userId);
        return ResponseEntity.ok().build();
    }

    // 답변 좋아요 취소
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeAnswer(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        answerService.unlikeAnswer(id, userId);
        return ResponseEntity.ok().build();
    }
}