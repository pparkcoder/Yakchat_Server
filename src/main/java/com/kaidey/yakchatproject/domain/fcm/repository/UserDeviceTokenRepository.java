package com.kaidey.yakchatproject.domain.fcm.repository;

import com.kaidey.yakchatproject.domain.fcm.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {
    List<UserDeviceToken> findByUserIdAndEnabledTrueAndReplyNotifyOptInTrue(Long userId);
    List<UserDeviceToken> findByUserIdAndEnabledTrueAndNotificationOptInTrue(Long userId);
    Optional<UserDeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
