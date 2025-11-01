package com.kaidey.yakchatproject.domain.material.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.domain.image.util.ImageUtils;
import com.kaidey.yakchatproject.domain.material.entity.Material;
import com.kaidey.yakchatproject.domain.material.repository.MaterialRepository;
import com.kaidey.yakchatproject.domain.material.request.MaterialCreateRequest;
import com.kaidey.yakchatproject.domain.material.response.MaterialResponse;
import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import com.kaidey.yakchatproject.domain.subject.repository.SubjectRepository;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.QuestionErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

	private final MaterialRepository materialRepository;
	private final UserRepository userRepository;
	private final SubjectRepository subjectRepository;
	private final ImageService imageService;
	private final ImageUtils imageUtils = new ImageUtils();

	@Override
	@Transactional
	public MaterialResponse createMaterial(MaterialCreateRequest request, Long userId) {
		Subject subject = subjectRepository.findById(request.getSubjectId())
			.orElseThrow(() -> new BusinessException(QuestionErrorCode.NOT_FOUND_SUBJECT));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

		Material material = Material.of(request, user, subject);

		List<String> keys = request.getUrlKey();
		if (keys != null && !keys.isEmpty()) {
			List<Image> imageList = imageService.saveMaterialImages(keys, material);
			material.addImage(imageList);
		}

		return MaterialResponse.of(materialRepository.save(material),
			imageUtils.convertToImageDtos(material.getImages()));
	}
}
