package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.repository.UserRepository;
import com.kaidey.yakchatproject.global.security.jwt.JwtTokenProvider;
import com.kaidey.yakchatproject.domain.user.entity.RoleType;
import com.kaidey.yakchatproject.domain.user.entity.GradeType;
import com.kaidey.yakchatproject.domain.user.repository.UserGradeRepository;
import com.kaidey.yakchatproject.domain.user.entity.UserGrade;
import com.kaidey.yakchatproject.domain.user.entity.UserType;
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
                       PasswordEncoder passwordEncoder,JwtTokenProvider jwtTokenProvider,
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
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUsername(userDto.getNickname());
        user.setRealName(userDto.getRealName());
        user.setUserType(userDto.getUserType());
        user.setAge(userDto.getAge());

        // 사용자 유형에 따라 필드 분기
        if (userDto.getUserType() == UserType.STUDENT) {
            user.setSchool(userDto.getSchool());
            user.setGrade(userDto.getGrade());
            user.setDepartment(userDto.getDepartment());
            user.setStudentId(userDto.getStudentId());
        } else if (userDto.getUserType() == UserType.PROFESSIONAL) {
            user.setLicenseNumber(userDto.getLicenseNumber());
            user.setLicenseIssuedDate(userDto.getLicenseIssuedDate());
        }

        Set<RoleType> roles = new HashSet<>();
        roles.add(RoleType.ROLE_USER); // 기본적으로 USER 역할 부여
        user.setRoles(roles);

        //  유저 저장 후 UserGrade 자동 생성
        User savedUser = userRepository.save(user);
        createUserGrade(savedUser);

        return savedUser;
    }

    // 사용자 로그인
    public Map<String, String> loginUser(UserDto userDto) {
        try {
            User user = userRepository.findByEmail(userDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", token);
            tokens.put("refresh_token", refreshToken);
            return tokens;
        } catch (Exception e) {
            throw new RuntimeException("Error logging in user: " + e.getMessage());
        }
    }




    // 토큰 갱신
    public Map<String, String> refreshToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 리프레시 토큰에서 사용자 정보 추출
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 새로운 액세스 토큰과 리프레시 토큰 생성
        String newAccessToken = jwtTokenProvider.generateToken(username, userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, userId);

        // 새로운 토큰들을 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);

        return tokens;
    }

    // 사용자 이름 중복 체크
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // 특정 사용자 조회
    public User getUserById(Long id) {
        try {
            return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user: " + e.getMessage());
        }
    }

    // 모든 사용자 조회
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all users: " + e.getMessage());
        }
    }

    // 사용자 정보 업데이트
    public User updateUser(Long id, UserDto userDto) {
        User user = getUserById(id);

        user.setUsername(userDto.getNickname());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRealName(userDto.getRealName());
        user.setAge(userDto.getAge());

        if (user.getUserType() == UserType.STUDENT) {
            user.setSchool(userDto.getSchool());
            user.setGrade(userDto.getGrade());
            user.setDepartment(userDto.getDepartment());
            user.setStudentId(userDto.getStudentId());
        } else if (user.getUserType() == UserType.PROFESSIONAL) {
            user.setLicenseNumber(userDto.getLicenseNumber());
            user.setLicenseIssuedDate(userDto.getLicenseIssuedDate());
        }

        return userRepository.save(user);
    }

    // 사용자 삭제
    public void deleteUser(Long id) {
        try {
            User user = getUserById(id);
            userGradeRepository.deleteById(user.getId()); // UserGrade도 함께 삭제
            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    @Transactional
    public void updateUserActivity(User user, int questions, int accepted, int likes, int purchases, int sales) {
        UserGrade userGrade = userGradeRepository.findByUserId(user.getId())
                .orElseGet(() -> createUserGrade(user)); // userGrade가 없으면 생성

        userGrade.setQuestionCount(userGrade.getQuestionCount() + questions);
        userGrade.setAcceptedCount(userGrade.getAcceptedCount() + accepted);
        userGrade.setLikeCount(userGrade.getLikeCount() + likes);
        userGrade.setPurchasedMaterialCount(userGrade.getPurchasedMaterialCount() + purchases);
        userGrade.setSoldMaterialCount(userGrade.getSoldMaterialCount() + sales);

        // 등급 업데이트
        gradeService.updateUserGrade(userGrade);
        userGradeRepository.save(userGrade);
    }

    private UserGrade createUserGrade(User user) {
        UserGrade newUserGrade = new UserGrade();
        newUserGrade.setUser(user);
        newUserGrade.setGrade(GradeType.GRAY);
        newUserGrade.setQuestionCount(0);
        newUserGrade.setAcceptedCount(0);
        newUserGrade.setLikeCount(0);
        newUserGrade.setPurchasedMaterialCount(0);
        newUserGrade.setSoldMaterialCount(0);

        return userGradeRepository.save(newUserGrade);
    }
}