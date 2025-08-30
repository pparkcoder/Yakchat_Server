package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.entity.*;
import com.kaidey.yakchatproject.domain.user.repository.UserGradeRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserGradeRepository userGradeRepository;
    private final GradeService gradeService;

    public UserService(UserRepository userRepository, GradeService gradeService,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                       UserGradeRepository userGradeRepository) {
        this.userRepository = userRepository;
        this.gradeService = gradeService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userGradeRepository = userGradeRepository;
    }

    // 사용자 등록
    @Transactional
    public User registerUser(UserDto userDto) {
        usernameExists(userDto.getUsername());
        emailExists(userDto.getEmail());

        UserType type = (userDto.getUserType() != null) ? userDto.getUserType() : UserType.STUDENT;

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // 비밀번호 암호화
        user.setSchool(userDto.getSchool());
        user.setGrade(userDto.getGrade());
        user.setAge(userDto.getAge());
        user.setUserType(type); // CHANGED: 저장

        // 역할 기본값
        Set<RoleType> roles = new HashSet<>();
        roles.add(RoleType.ROLE_USER);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        createUserGrade(savedUser);

        return savedUser;
    }

    // 사용자 로그인
    public Map<String, String> loginUser(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.NOT_MATCHES_PASSWORD);
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", token);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    // 토큰 갱신
    public Map<String, String> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(UserErrorCode.INVAILD_REFRESH_TOKEN);
        }
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateToken(username, userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, userId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);
        return tokens;
    }

    // 사용자 이름 중복 체크
    public void usernameExists(String username) {
        Optional<User> findUserName = userRepository.findByUsername(username);
        if (findUserName.isPresent()) {
            throw new BusinessException(UserErrorCode.ALREAD_EXIST_NAME);
        }
    }

    // 이메일 중복 체크(읽기 전용)
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.existsByEmail(email);
    }

    public void emailExists(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(UserErrorCode.INVALID_EMAIL);   // ← 추가
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.ALREADY_EXIST_EMAIL);
        }
    }


    // 특정 사용자 조회
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));
    }

    // 모든 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 사용자 정보 업데이트
    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        try {
            User user = getUserById(id);
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setSchool(userDto.getSchool());
            user.setGrade(userDto.getGrade());
            user.setAge(userDto.getAge());
            if (userDto.getUserType() != null) {
                user.setUserType(userDto.getUserType());
            }
            return userRepository.save(user);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(Long id) {
        try {
            User user = getUserById(id);

            // CHANGED: UserGrade PK != userId일 수 있으니 안전하게 조회 후 삭제
            userGradeRepository.findByUserId(user.getId())
                    .ifPresent(userGradeRepository::delete);

            userRepository.delete(user);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    @Transactional
    public void updateUserActivity(User user, int questions, int accepted, int likes, int purchases, int sales) {
        try {
            UserGrade userGrade = userGradeRepository.findByUserId(user.getId())
                    .orElseGet(() -> createUserGrade(user));

            userGrade.setQuestionCount(userGrade.getQuestionCount() + questions);
            userGrade.setAcceptedCount(userGrade.getAcceptedCount() + accepted);
            userGrade.setLikeCount(userGrade.getLikeCount() + likes);
            userGrade.setPurchasedMaterialCount(userGrade.getPurchasedMaterialCount() + purchases);
            userGrade.setSoldMaterialCount(userGrade.getSoldMaterialCount() + sales);

            gradeService.updateUserGrade(userGrade);
            userGradeRepository.save(userGrade);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    @Transactional
    public UserGrade createUserGrade(User user) {
        try {
            UserGrade newUserGrade = new UserGrade();
            newUserGrade.setUser(user);
            newUserGrade.setGrade(GradeType.GRAY);
            newUserGrade.setQuestionCount(0);
            newUserGrade.setAcceptedCount(0);
            newUserGrade.setLikeCount(0);
            newUserGrade.setPurchasedMaterialCount(0);
            newUserGrade.setSoldMaterialCount(0);
            user.setUserGrade(newUserGrade);

            return userGradeRepository.save(newUserGrade);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }
}
