package com.kaidey.yakchatproject.domain.s3.service;

import java.net.URL;
import java.util.List;

import com.amazonaws.HttpMethod;
import com.kaidey.yakchatproject.domain.s3.dto.S3Dto;

public interface S3ServiceV2 {

	URL generatePresignedUrl(String objectKey, HttpMethod method);

	// PutObject 요청에 대한 Presigned URL 생성
	List<S3Dto> generatePresignedPutUrl(List<String> files, Long userId, String type);

	// GetObject 요청에 대한 Presigned URL 생성
	List<S3Dto> generatePresignedGetUrl(List<String> keys);

	// DeleteObject 요청에 대한 Presigned URL 생성
	List<S3Dto> generatePresignedDeleteUrl(List<String> keys);

	String uploadDocumentImage(byte[] fileData,
		Long userId,
		String documentType,
		String fileName,
		String contentType);

}
