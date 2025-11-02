package com.kaidey.yakchatproject.domain.material.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.kaidey.yakchatproject.domain.material.entity.Material;

@Repository
public interface MaterialRepository extends CrudRepository<Material, Long> {

	List<Material> findByUserIdOrderByCreatedAt(Long userId);
}
