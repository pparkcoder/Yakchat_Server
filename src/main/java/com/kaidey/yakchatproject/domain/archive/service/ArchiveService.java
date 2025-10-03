package com.kaidey.yakchatproject.domain.archive.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.answer.dto.AnswerDto;
import com.kaidey.yakchatproject.domain.question.dto.QuestionDto;
import com.kaidey.yakchatproject.domain.scrap.dto.ScrapDto;

public interface ArchiveService {

	ScrapDto creatQuestionScrap(ScrapDto scrapDto);

	ScrapDto creatAnswerScrap(ScrapDto scrapDto);

	List<QuestionDto> getQuestionScraps(Long scraperId);

	List<AnswerDto> getAnswerScraps(Long scraperId);
}
