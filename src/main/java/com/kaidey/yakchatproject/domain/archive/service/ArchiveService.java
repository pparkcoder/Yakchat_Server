package com.kaidey.yakchatproject.domain.archive.service;

import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.question.repository.QuestionRepository;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;
import com.kaidey.yakchatproject.domain.scrap.entity.Scrap;
import com.kaidey.yakchatproject.domain.scrap.repository.ScrapRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.OffsetScrollPositionArgumentResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArchiveService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ScrapRepository scrapRepository;
    private final OffsetScrollPositionArgumentResolver offsetScrollPositionArgumentResolver;

    @Autowired
    public ArchiveService(UserRepository userRepository, QuestionRepository questionRepository, ScrapRepository scrapRepository, OffsetScrollPositionArgumentResolver offsetScrollPositionArgumentResolver) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.scrapRepository = scrapRepository;
        this.offsetScrollPositionArgumentResolver = offsetScrollPositionArgumentResolver;
    }

    // 스크랩
    @Transactional
    public ScrapDto createScrap(ScrapDto scrapDto) {
        Question question = questionRepository.findById(scrapDto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        // 이미 스크랩 되어 있는지 확인
        if(scrapRepository.findByUserIdAndQuestionId(scrapDto.getScraperId(), scrapDto.getQuestionId()).size() > 0) {
            throw new IllegalStateException("이미 스크랩 되었습니다.");
        }

        // 스크랩
        Scrap scrap = new Scrap();
        scrap.setQuestion(question);
        scrap.setScraperId(scrapDto.getScraperId());
        Scrap savedScrap = scrapRepository.save(scrap);
        return convertToScrapDto(savedScrap);
    }

    // 스크랩 보기
    @Transactional
    public List<QuestionDto> getScrapsByUserId(Long scraperId){
        return scrapRepository.findByUserIdCreatedAtDesc(scraperId).stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());


    }

    private ScrapDto convertToScrapDto(Scrap scrap) {
        ScrapDto scrapDto = new ScrapDto();
        scrapDto.setId(scrap.getId());
        scrapDto.setQuestionId(scrap.getQuestion().getId());
        scrapDto.setScraperId(scrap.getScraperId());
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
        questionDto.setUserName(scrap.getQuestion().getUser().getUsername());
        questionDto.setCreatedAt(scrap.getQuestion().getCreatedAt());
        questionDto.setLikeCount(scrap.getQuestion().getLikes());
        questionDto.setViewCount(scrap.getQuestion().getViewCount());

        return questionDto;
    }
}
