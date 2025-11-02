package com.kaidey.yakchatproject.domain.image.entity;

import com.kaidey.yakchatproject.domain.answer.entity.Answer;
import com.kaidey.yakchatproject.domain.image.enums.ImageType;
import com.kaidey.yakchatproject.domain.material.entity.Material;
import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private Long id;

	@Column(nullable = false)
	private String urlKey;

	private String mime; // MIME 타입

	@ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
	@JoinColumn(name = "question_id", nullable = true)
	private Question question; // 연관된 질문 (nullable)

	@ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
	@JoinColumn(name = "answer_id", nullable = true)
	private Answer answer; // 연관된 답변 (nullable)

	@ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
	@JoinColumn(name = "user_id", nullable = true)
	private User user; // 연관된 사용자 (nullable)

	@ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
	@JoinColumn(name = "material_id", nullable = true)
	private Material material; // 연관된 학습자료 (nullable)

	private int stepIndex;

	@Enumerated(EnumType.STRING) // Default : ORDINAL(숫자로 들어감) -> 중간에 상태가 추가되면 기존에 숫자가 유지되므로 상태가 꼬임
	private ImageType imageType;
}