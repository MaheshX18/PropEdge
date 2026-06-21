package com.mvp18.trading_challenge_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // Get email from JWT token (set by JwtAuthenticationFilter)
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        return authentication.getPrincipal().toString();
    }
}