package com.foodorder.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler xử lý khi đăng nhập OAuth2 (Google/Facebook) thất bại
 * Redirect về frontend với error code
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.oauth2-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String errorCode = "OAUTH2_LOGIN_FAILED";

        // Map lỗi cụ thể thành error code
        String errorMessage = exception.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("access_denied")) {
                errorCode = "OAUTH2_ACCESS_DENIED";
            } else if (errorMessage.contains("invalid_token")) {
                errorCode = "OAUTH2_INVALID_TOKEN";
            } else if (errorMessage.contains("expired")) {
                errorCode = "OAUTH2_TOKEN_EXPIRED";
            }
        }

        String redirectUrl = frontendRedirectUrl + "?error=" +
                URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        log.info("Redirecting to frontend with error: {}", errorCode);
        response.sendRedirect(redirectUrl);
    }
}

