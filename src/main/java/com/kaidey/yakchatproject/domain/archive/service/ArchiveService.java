package com.kaidey.yakchatproject.domain.archive.service;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.answer.repository.AnswerRepository;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.question.repository.QuestionRepository;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;
import com.kaidey.yakchatproject.domain.scrap.entity.Scrap;
import com.kaidey.yakchatproject.domain.scrap.repository.ScrapRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.OffsetScrollPositionArgumentResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ScrapRepository scrapRepository;
    private final OffsetScrollPositionArgumentResolver offsetScrollPositionArgumentResolver;

    // 질문 스크랩
    @Transactional
    public ScrapDto creatQuestionScrap(ScrapDto scrapDto) {
        Question question = questionRepository.findById(scrapDto.getQuestionId())
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

        // 이미 스크랩 되어 있는지 확인
        if(scrapRepository.findByUserIdAndQuestionId(scrapDto.getScraperId(), scrapDto.getQuestionId()).size() > 0) {
            throw new BusinessException(ArchiveErrorCode.ALREADY_SCRAP);
        }

        Scrap scrap = new Scrap();
        scrap.setQuestion(question);
        scrap.setScraperId(scrapDto.getScraperId());
        Scrap savedScrap = scrapRepository.save(scrap);
        return convertToScrapDto(savedScrap);
    }

    // 답변 스크랩
    @Transactional
    public ScrapDto creatAnswerScrap(ScrapDto scrapDto) {
        Answer answer = answerRepository.findById(scrapDto.getAnswerId())
                .orElseThrow(() -> new BusinessException(AnswerErrorCode.NOT_FOUND_ANSWER));

        Question question = questionRepository.findById(scrapDto.getQuestionId())
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_QUESTION));

        // 이미 스크랩 되어 있는지 확인
        if(scrapRepository.findByUserIdAndAnswerId(scrapDto.getScraperId(), scrapDto.getAnswerId()).size() > 0) {
            throw new BusinessException(ArchiveErrorCode.ALREADY_SCRAP);
        }


        Scrap scrap = new Scrap();
        scrap.setAnswer(answer);
        scrap.setQuestion(question);
        scrap.setScraperId(scrapDto.getScraperId());
        Scrap savedScrap = scrapRepository.save(scrap);
        return convertToScrapDto(savedScrap);
    }

    // 질문 스크랩 보기
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionScraps(Long scraperId) {
        return scrapRepository.findByUserIdCreatedAtDesc(scraperId).stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());
    }

    // 답변 스크랩 보기
    @Transactional(readOnly = true)
    public List<AnswerDto> getAnswerScraps(Long scraperId){
        return scrapRepository.findByUserIdCreatedAtDesc2(scraperId).stream()
                .map(this::convertToAnswerDto)
                .collect(Collectors.toList());
    }

    private ScrapDto convertToScrapDto(Scrap scrap) {
        ScrapDto scrapDto = new ScrapDto();
        scrapDto.setId(scrap.getId());
        scrapDto.setScraperId(scrap.getScraperId());
        scrapDto.setQuestionId(scrap.getQuestion().getId());
        scrapDto.setAnswerId(scrap.getAnswer() != null ? scrap.getAnswer().getId() : null);
        return scrapDto;
    }

    private QuestionDto convertToQuestionDto(Scrap scrap) {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setId(scrap.getQuestion().getId());
        questionDto.setTitle(scrap.getQuestion().getTitle());
        questionDto.setContent(scrap.getQuestion().getContent());
        questionDto.setSubjectId(scrap.getQuestion().getSubject().getId());
        questionDto.setSubjectName(scrap.getQuestion().getSubject().getName());
        questionDto.setUserId(scrap.getQuestion().getUser().getId());
        questionDto.setNickname(scrap.getQuestion().getUser().getNickname());
        questionDto.setCreatedAt(scrap.getQuestion().getCreatedAt());
        questionDto.setLikeCount(scrap.getQuestion().getLikes());
        questionDto.setViewCount(scrap.getQuestion().getViewCount());

        return questionDto;
    }

    private AnswerDto convertToAnswerDto(Scrap scrap) {
        AnswerDto answerDto = new AnswerDto();
        answerDto.setId(scrap.getAnswer().getId());
        answerDto.setContent(scrap.getAnswer().getContent());
        answerDto.setQuestionId(scrap.getQuestion().getId());
        answerDto.setUserId(scrap.getAnswer().getUser().getId());
        answerDto.setUserName(scrap.getAnswer().getUser().getUsername());
        answerDto.setCreatedAt(scrap.getAnswer().getCreatedAt());
        answerDto.setModifiedAt(scrap.getAnswer().getModifiedAt());
        answerDto.setLikeCount(scrap.getAnswer().getLikes());
        answerDto.setAccepted(scrap.getAnswer().getIsAccepted());
        return answerDto;
    }
}
