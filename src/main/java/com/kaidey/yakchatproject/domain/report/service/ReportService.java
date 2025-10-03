package com.kaidey.yakchatproject.domain.report.service;

import java.util.List;

import com.kaidey.yakchatproject.domain.report.dto.ReportDto;
import com.kaidey.yakchatproject.domain.report.entity.ReportStatus;

public interface ReportService {

	ReportDto createReport(ReportDto reportDto);

	List<ReportDto> getAllReports();

	ReportDto updateReportStatus(Long id, ReportStatus status, String handler, String result);

}