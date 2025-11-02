package com.kaidey.yakchatproject.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.enums.ImageType;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

	List<Image> findByUserIdAndImageType(Long userId, ImageType imageType);
}
