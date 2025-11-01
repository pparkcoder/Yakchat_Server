package com.kaidey.yakchatproject.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kaidey.yakchatproject.domain.image.entity.Image;
import com.kaidey.yakchatproject.domain.image.enums.ImageType;
import com.kaidey.yakchatproject.domain.onboarding.entity.StudentProfile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE `user` SET `is_deleted` = 1, `deleted_at` = NOW() WHERE `user_id` = ?")
@Where(clause = "is_deleted = 0")
@Table(name = "user")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	/** 실명(중복 허용) */
	@Column(nullable = false, length = 50)
	private String username;

	@Column(nullable = false, length = 50)
	private String nickname;

	/** 이메일(활성 사용자 한정 유니크: DB에서 (email, is_deleted) 복합 유니크) */
	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserType userType = UserType.STUDENT;

	@Column(nullable = false)
	private String school;

	/** 계정 활성 플래그(정지/휴면과 분리) */
	@Column(nullable = false)
	private Boolean isActive = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	/** 소프트 삭제 플래그/시각 */
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	private LocalDateTime lastLoginAt;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private StudentProfile studentProfile;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private UserGrade userGrade;

	@BatchSize(size = 100)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<Image> images = new ArrayList<>();

	private LocalDateTime lastNicknameChangedAt;

	/** 역할(선택) */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private Set<RoleType> roles = new HashSet<>();

	/** 권한 문자열 컬렉션(선택) */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
	private List<String> authorities = new ArrayList<>();

	/* ===== UserDetails 구현 ===== */

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities.stream()
			.map(auth -> (GrantedAuthority)() -> auth)
			.toList();
	}

	/** 주의: 로그인 아이디로 email을 쓰는 경우, UserDetailsService에서 email로 조회하도록 구현하세요. */
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/** 활성 + 미삭제 사용자만 로그인 가능 */
	@Override
	public boolean isEnabled() {
		return Boolean.TRUE.equals(isActive) && !isDeleted;
	}

	/* ===== 도메인 메서드 ===== */

	public void update(String nickname) {
		this.nickname = nickname;
		this.lastNicknameChangedAt = LocalDateTime.now();
	}

	public void updateWithImage(String nickname, List<Image> images) {
		this.nickname = nickname;
		this.lastNicknameChangedAt = LocalDateTime.now();

		// 기존 프로필 이미지 제거(P 타입만 교체)
		Iterator<Image> it = this.images.iterator();
		while (it.hasNext()) {
			Image img = it.next();
			if (img.getImageType() == ImageType.PROFILE) {
				img.setUser(null);
				it.remove();
			}
		}
		// 신규 이미지 연결
		for (Image image : images) {
			this.images.add(image);
			image.setUser(this);
		}
	}
}
