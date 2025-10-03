package com.kaidey.yakchatproject.domain.user.service;

import java.util.List;
import java.util.Map;

import com.kaidey.yakchatproject.domain.user.dto.PromotionDto;
import com.kaidey.yakchatproject.domain.user.dto.UserDto;
import com.kaidey.yakchatproject.domain.user.entity.User;
import com.kaidey.yakchatproject.domain.user.entity.UserGrade;

public interface UserService {

	User registerUser(UserDto userDto);

	Map<String, String> loginUser(UserDto userDto);

	Map<String, String> refreshToken(String refreshToken);

	void usernameExists(String username);

	boolean existsByEmail(String email);

	void emailExists(String email);

	User getUserById(Long id);

	List<User> getAllUsers();

	User updateUser(Long id, UserDto userDto);

	void deleteUser(Long id);

	void updateUserActivity(User user, int questionDelta, int answerDelta);

	void incrementAcceptedCount(User user, int delta);

	UserGrade createUserGrade(User user);

	PromotionDto getPromotion(Long userId);

}
