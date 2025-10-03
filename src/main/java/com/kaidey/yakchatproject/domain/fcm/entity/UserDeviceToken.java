package com.kaidey.yakchatproject.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 20)
    private String platform = "web";

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean notificationOptIn = true;

    @Column(nullable = false)
    private Boolean replyNotifyOptIn = true;

    private LocalDateTime lastSeenAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}