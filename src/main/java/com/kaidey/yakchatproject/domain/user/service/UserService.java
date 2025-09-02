package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.dto.PromotionDto;
import com.kaidey.yakchatproject.domain.user.entity.*;
import com.kaidey.yakchatproject.domain.user.repository.UserGradeRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserGradeRepository userGradeRepository;
    private final GradeService gradeService;

    // 사용자 등록
    @Transactional
    public User registerUser(UserDto userDto) {
        emailExists(userDto.getEmail());

        UserType type = (userDto.getUserType() != null) ? userDto.getUserType() : UserType.STUDENT;

        // 닉네임 중복만 체크
        String nickname = userDto.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = userDto.getRealName(); // 닉네임 없으면 실명 기반
        }
        if (userRepository.existsByNicknameIgnoreCase(nickname)) {
            throw new BusinessException(UserErrorCode.NICKNAME_TAKEN);
        }

        User user = new User();
        user.setUsername(userDto.getRealName());
        user.setNickname(nickname);
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setSchool(userDto.getSchool());
        user.setGrade(userDto.getGrade());
        user.setUserType(type);

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

        String token = jwtTokenProvider.generateToken(user.getNickname(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getNickname(), user.getId());
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
            throw new BusinessException(UserErrorCode.NICKNAME_TAKEN);
        }
    }

    // 이메일 중복 체크(읽기 전용)
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return userRepository.existsByEmail(email);
    }

    // 이메일 중복 체크 및 유효성 검사
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
            user.setUsername(userDto.getRealName());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setSchool(userDto.getSchool());
            user.setGrade(userDto.getGrade());
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


    //질문·답변 활동 기록을 갱신, 등급 업데이트까지 처리
    @Transactional
    public void updateUserActivity(User user, int questionDelta, int answerDelta) {
        try {
            UserGrade g = userGradeRepository.findByUserId(user.getId())
                    .orElseGet(() -> createUserGrade(user));
            g.setQuestionCount(g.getQuestionCount() + questionDelta);
            g.setAnswerCount(g.getAnswerCount() + answerDelta);
            gradeService.updateUserGrade(g);
            userGradeRepository.save(g);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    // 채택된 답변 수만 증가시키는 단순 카운트 증가용 메서드
    @Transactional
    public void incrementAcceptedCount(User user, int delta) {
        UserGrade g = userGradeRepository.findByUserId(user.getId())
                .orElseGet(() -> createUserGrade(user));
        g.setAcceptedCount(g.getAcceptedCount() + delta);
        userGradeRepository.save(g);
    }


    @Transactional
    public UserGrade createUserGrade(User user) {
        try {
            UserGrade g = new UserGrade();
            g.setUser(user);
            g.setGrade(GradeType.NONE);
            g.setQuestionCount(0);
            g.setAnswerCount(0);
            g.setAcceptedCount(0);
            g.setLikeCount(0);
            g.setPurchasedMaterialCount(0);
            g.setSoldMaterialCount(0);
            user.setUserGrade(g);
            return userGradeRepository.save(g);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }

    public PromotionDto getPromotion(Long userId) {
        User user = getUserById(userId);
        UserGrade g = userGradeRepository.findByUserId(user.getId())
                .orElseGet(() -> createUserGrade(user));
        gradeService.updateUserGrade(g);

        GradeService.NextPromotion np = gradeService.getNextPromotion(g);
        return new PromotionDto(
                toKorean(g.getGrade()),
                np.getNextGrade() == null ? null : toKorean(np.getNextGrade()),
                np.getProgress(),
                np.getTarget(),
                np.progressRate()
        );
    }

    private String toKorean(GradeType t) {
        return switch (t) {
            case NONE -> "무등급";
            case SESSAK -> "새싹";
            case HANAL -> "한알";
            case DUAL -> "두알";
            case GOSU -> "고수";
            case MYEONGYAK -> "명약";
        };
    }
}
