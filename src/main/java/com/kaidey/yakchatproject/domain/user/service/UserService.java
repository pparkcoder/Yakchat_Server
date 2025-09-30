package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.dto.PromotionDto;
import com.kaidey.yakchatproject.domain.user.dto.DeleteAccountRequest;
import com.kaidey.yakchatproject.domain.user.entity.*;
import com.kaidey.yakchatproject.domain.user.repository.UserGradeRepository;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.domain.fcm.repository.UserDeviceTokenRepository;
import com.kaidey.yakchatproject.domain.image.service.ImageService;
import com.kaidey.yakchatproject.global.exception.BusinessException;
import com.kaidey.yakchatproject.global.exception.CommonErrorCode;
import com.kaidey.yakchatproject.global.exception.UserErrorCode;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import com.kaidey.yakchatproject.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserGradeRepository userGradeRepository;
    private final GradeService gradeService;
    private final RedisUtil redisUtil;

    private final UserDeviceTokenRepository fcmTokenRepository;   // 구현/패키지에 맞게 추가
    private final ImageService imageService;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration; // 기본 1시간

    @Value("${jwt.refreshExpiration:604800000}")
    private long refreshExpiration; // 기본 7일

    // 사용자 등록
    @Transactional
    public User registerUser(UserDto userDto) {
        emailExists(userDto.getEmail());

        UserType type = (userDto.getUserType() != null) ? userDto.getUserType() : UserType.STUDENT;

        // 닉네임 중복 체크 (삭제 유저 제외)
        String nickname = userDto.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = userDto.getRealName();
        }
        if (userRepository.existsByNicknameIgnoreCaseAndIsDeletedFalse(nickname)) {
            throw new BusinessException(UserErrorCode.NICKNAME_TAKEN);
        }

        User user = new User();
        user.setUsername(userDto.getRealName());
        user.setNickname(nickname);
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setSchool(userDto.getSchool());
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
        User user = userRepository.findByEmailAndIsDeletedFalse(userDto.getEmail())
                .orElseThrow(() -> new BusinessException(UserErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.NOT_MATCHES_PASSWORD);
        }
        if (!user.isEnabled()) {
            throw new DisabledException("deleted or inactive user");
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

    /**
     * 계정 즉시 완전 삭제
     */
    @Transactional
    public void deleteAccountImmediately(Long userId, DeleteAccountRequest request) {
        User user = getUserById(userId);

        // (선택) 비밀번호 확인 유지 가능
        // if (request.getPassword() != null && !request.getPassword().isEmpty()) {
        //     if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //         throw new BusinessException(UserErrorCode.NOT_MATCHES_PASSWORD);
        //     }
        // }

        softDeleteAccount(user);
        log.info("계정 소프트 삭제 완료 - userId: {}", userId);
    }

    @Transactional
    protected void softDeleteAccount(User user) {
        Long userId = user.getId();
        try {
            // 1) nickname만 고유하게 익명화 (UNIQUE 회피용)
            String suffix = userId + "_" + System.currentTimeMillis();
            user.setNickname("탈퇴한 사용자_" + suffix);
            user.setUsername("deleted_user_" + userId);

            // 3) 기타 PII/프로필
            user.setSchool(null);

//            // 4) 연결 자원 정리
//            fcmTokenRepository.deleteByUserId(userId);
//            tokenService.revokeAllTokensForUser(userId);
//            imageService.deleteProfileImagesByUserId(userId);

            // 5) 소프트 삭제 트리거 (@SQLDelete가 UPDATE로 변환)
            userRepository.delete(user);

            log.info("계정 소프트 삭제 처리 완료 - userId: {}", userId);
        } catch (Exception e) {
            log.error("계정 소프트 삭제 실패 - userId: {}", userId, e);
            throw new BusinessException(CommonErrorCode.COMMON_ERROR);
        }
    }



    // ===== 유틸리티 메서드들 =====

    private long getRemainingTokenTime(String token) {
        try {
            // JWT에서 만료 시간을 추출하여 남은 시간 계산
            return jwtTokenProvider.validateToken(token) ? jwtExpiration : 0;
        } catch (Exception e) {
            return jwtExpiration; // 기본값 사용
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() <= 2) {
            return "*".repeat(localPart.length()) + "@" + domainPart;
        }

        return localPart.charAt(0) + "*".repeat(localPart.length() - 2) +
                localPart.charAt(localPart.length() - 1) + "@" + domainPart;
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
            throw new BusinessException(UserErrorCode.INVALID_EMAIL);
        }
        if (userRepository.existsByEmailAndIsDeletedFalse(email)) {
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
            case GREEN -> "TEST";
        };
    }
}
