package com.kaidey.yakchatproject.domain.fcm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTokenRequest {
    private String token;
    private String platform = "web";
    private Boolean replyNotifyOptIn = true;
}
