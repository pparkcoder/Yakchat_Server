package com.kaidey.yakchatproject.domain.user.repository;

import com.kaidey.yakchatproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // 아이디로 사용자 조회
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회
    boolean existsByUsername(String username); // 아이디 중복 체크
    boolean existsByEmail(String email); // 이메일 중복 체크
}
