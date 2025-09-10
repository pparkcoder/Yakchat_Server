package com.kaidey.yakchatproject.global.util;

import com.kaidey.yakchatproject.domain.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            return u.getId();
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass());
    }
}
