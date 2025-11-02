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
import com.kaidey.yakchatproject.global.exception.MaterialErrorCode;
import com.kaidey.yakchatproject.global.exception.QuestionErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

		return buildMaterialResponse(materialRepository.save(material));
	}

	@Override
	public MaterialResponse getMaterialById(Long id) {
		Material material = materialRepository.findById(id)
			.orElseThrow(() -> new BusinessException(MaterialErrorCode.NOT_FOUND_MATERIAL));

		return buildMaterialResponse(material);
	}

	@Override
	public MaterialResponse getMaterialByUserId(Long userId) {
		Material material = materialRepository.findByUserIdOrderByCreatedAt(userId)
			.orElseThrow(() -> new BusinessException(MaterialErrorCode.NOT_FOUND_MATERIAL));

		return buildMaterialResponse(material);
	}

	@Override
	@Transactional
	public Boolean deleteMaterialById(Long id) {
		if (!materialRepository.existsById(id)) {
			throw new BusinessException(MaterialErrorCode.NOT_FOUND_MATERIAL);
		}
		materialRepository.deleteById(id);
		return true;
	}

	private MaterialResponse buildMaterialResponse(Material material) {
		return MaterialResponse.of(material, imageUtils.convertToImageDtos(material.getImages()));
	}
}
