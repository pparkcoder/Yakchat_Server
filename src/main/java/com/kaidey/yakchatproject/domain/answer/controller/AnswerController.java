package com.kaidey.yakchatproject.domain.answer.controller;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.service.AnswerService;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.service.UserService;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<AnswerDto> createAnswer(
            @RequestParam("content") String content,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "keys", required = false) List<String> keys,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        AnswerDto answerDto = new AnswerDto(content, questionId, userId);
        AnswerDto newAnswer = answerService.createAnswer(answerDto, keys);

        return ResponseEntity.ok(newAnswer);
    }


    // 특정 답변 조회
    @GetMapping("/{id}")
    public ResponseEntity<AnswerDto> getAnswerById(@PathVariable Long id) {
        AnswerDto answer = answerService.getAnswerById(id);
        return ResponseEntity.ok(answer);
    }

    // 특정 질문과 사용자에 대한 답변 조회
//    @GetMapping("/question/{questionId}/user")
//    public ResponseEntity<?> getAnswersByQuestionIdAndUserId(
//            @PathVariable Long questionId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            if (userDetails == null) {
//                logger.warn("Authentication failed: UserDetails is null.");
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("User is not authenticated.");
//            }
//
//            Long userId;
//            if (userDetails instanceof User) {
//                userId = ((User) userDetails).getId();
//            } else {
//                logger.error("UserDetails is not an instance of User.");
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("Internal server error.");
//            }
//            logger.info("Fetching answers for questionId: {} and userId: {}", questionId, userId);
//
//            List<AnswerDto> answerDtos = answerService.getAnswersByQuestionIdAndUserId(questionId, userId);
//            return ResponseEntity.ok(answerDtos);
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid argument: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Invalid argument: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error occurred while fetching answers: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("An unexpected error occurred.");
//        }
//    }
    // 모든 답변 조회
    @GetMapping
    public ResponseEntity<List<AnswerDto>> getAllAnswers() {
        List<AnswerDto> answers = answerService.getAllAnswers();
        return ResponseEntity.ok(answers);
    }

    // 특정 질문에 해당하는 답변 조회
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerDto>> getAnswersByQuestionId(@PathVariable Long questionId) {
        List<AnswerDto> answers = answerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(answers);
    }

    // 답변 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<AnswerDto> updateAnswer(
            @PathVariable Long id,
            @RequestParam("content") String content,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "keys", required = false) List<String> keys,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        AnswerDto answerDto = new AnswerDto(content, questionId, userId);
        AnswerDto updatedAnswer = answerService.updateAnswer(id, answerDto, keys);

        return ResponseEntity.ok(updatedAnswer);
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