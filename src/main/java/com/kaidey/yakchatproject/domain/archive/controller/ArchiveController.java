package com.kaidey.yakchatproject.domain.archive.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.service.AnswerService;
import com.kaidey.yakchatproject.domain.archive.service.ArchiveService;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;
import com.kaidey.yakchatproject.domain.material.service.MaterialService;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionWithAnswersDto;
import com.kaidey.yakchatproject.domain.question.service.QuestionService;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

	private final JwtTokenProvider jwtTokenProvider;
	private final ArchiveService archiveService;
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final MaterialService materialService;

	// 내 질문 조회(최신순 5개)
	@GetMapping("/my-questions")
	public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllQuestions(
		@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		List<QuestionWithAnswersDto> questions = questionService.getQuestionsByUserIdByCreatedAtDesc(userId);
		return ResponseEntity.ok(questions);
	}

	// 내 답변 조회(최신순 5개)
	@GetMapping("/my-answers")
	public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllAnswers(@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		List<QuestionWithAnswersDto> answers = answerService.getAnswersByUserIdByCreatedAtDesc(userId);
		return ResponseEntity.ok(answers);
	}

	// 내 학습자료 조회
	@GetMapping("/my-materials")
	public ResponseEntity<List<MaterialResponse>> getMyMaterial(@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		return ResponseEntity.ok(materialService.getMaterialByUserId(userId));
	}

	// 채택 답변이 있는 질문 조회(최신순 5개)
	@GetMapping("/my-questions/accepted")
	public ResponseEntity<List<QuestionWithAnswersDto>> getMyAllQuestionsByAccepted(
		@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		List<QuestionWithAnswersDto> questions = questionService.getQuestionsByUserIdByAcceptedByCreatedAtDesc(userId);
		return ResponseEntity.ok(questions);
	}

	// 질문 스크랩
	@PostMapping("/question-scraps")
	public ResponseEntity<ScrapDto> creatQuestionScrap(
		@RequestParam("questionId") Long questionId,
		@RequestHeader("Authorization") String token) {

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
		@RequestHeader("Authorization") String token) {

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
	public ResponseEntity<List<QuestionDto>> getQuestionScraps(@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		List<QuestionDto> scraps = archiveService.getQuestionScraps(userId);
		return ResponseEntity.ok(scraps);
	}

	// 답변 스크랩 보기
	@GetMapping("/answer-scraps")
	public ResponseEntity<List<AnswerDto>> getAnswerScrap(@RequestHeader("Authorization") String token) {
		Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
		List<AnswerDto> scraps = archiveService.getAnswerScraps(userId);
		return ResponseEntity.ok(scraps);
	}
}