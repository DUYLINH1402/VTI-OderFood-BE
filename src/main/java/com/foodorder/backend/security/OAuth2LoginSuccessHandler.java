package com.foodorder.backend.security;

import com.foodorder.backend.auth.service.OAuth2UserService;
import com.foodorder.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handler xử lý khi đăng nhập OAuth2 (Google/Facebook) thành công
 * Thực hiện:
 * 1. Xác định provider (google/facebook)
 * 2. Lấy thông tin user từ OAuth2 response
 * 3. Gọi OAuth2UserService để xử lý user trong transaction
 * 4. Redirect về frontend kèm JWT token trong URL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2UserService oAuth2UserService;
    private final UserRepository userRepository;

    /**
     * URL frontend để redirect sau khi đăng nhập thành công
     */
    @Value("${app.frontend.oauth2-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 login success, processing authentication...");

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();
        String registrationId = authToken.getAuthorizedClientRegistrationId(); // "google" hoặc "facebook"

        log.info("OAuth2 Provider: {}", registrationId);

        // Trích xuất thông tin user dựa trên provider
        String email;
        String fullName;
        String avatarUrl;
        boolean emailVerified;
        String authProvider;

        if ("google".equals(registrationId)) {
            // Google OAuth2 response format
            email = oAuth2User.getAttribute("email");
            fullName = oAuth2User.getAttribute("name");
            avatarUrl = oAuth2User.getAttribute("picture");
            Boolean verified = oAuth2User.getAttribute("email_verified");
            emailVerified = verified != null && verified;
            authProvider = "GOOGLE";

            log.info("Google OAuth2 user info - Email: {}, Name: {}", email, fullName);
        } else if ("facebook".equals(registrationId)) {
            // Facebook OAuth2 response format
            email = oAuth2User.getAttribute("email");
            fullName = oAuth2User.getAttribute("name");

            // Facebook trả về picture dưới dạng nested object
            Map<String, Object> pictureObj = oAuth2User.getAttribute("picture");
            if (pictureObj != null && pictureObj.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pictureData = (Map<String, Object>) pictureObj.get("data");
                avatarUrl = (String) pictureData.get("url");
            } else {
                avatarUrl = null;
            }

            // Facebook không trả về email_verified, nhưng nếu có email thì đã được xác minh
            emailVerified = email != null && !email.isEmpty();
            authProvider = "FACEBOOK";

            log.info("Facebook OAuth2 user info - Email: {}, Name: {}", email, fullName);
        } else {
            log.error("Unsupported OAuth2 provider: {}", registrationId);
            String errorUrl = frontendRedirectUrl + "?error=" +
                    URLEncoder.encode("UNSUPPORTED_OAUTH2_PROVIDER", StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
            return;
        }

        // Kiểm tra email có tồn tại không (Facebook có thể không cấp quyền email)
        if (email == null || email.isEmpty()) {
            log.warn("Email not provided by {} for user", registrationId);
            String errorUrl = frontendRedirectUrl + "?error=" +
                    URLEncoder.encode("OAUTH2_EMAIL_REQUIRED", StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
            return;
        }

        // Kiểm tra email đã được xác minh chưa
        if (!emailVerified) {
            log.warn("{} email not verified for user: {}", registrationId, email);
            String errorCode = "google".equals(registrationId) ? "GOOGLE_EMAIL_NOT_VERIFIED" : "FACEBOOK_EMAIL_NOT_VERIFIED";
            String errorUrl = frontendRedirectUrl + "?error=" +
                    URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
            return;
        }

        try {
            // Kiểm tra tài khoản có bị khóa không (nếu đã tồn tại)
            var existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().isActive()) {
                log.warn("User account is locked: {}", email);
                String errorUrl = frontendRedirectUrl + "?error=" +
                        URLEncoder.encode("USER_LOCKED", StandardCharsets.UTF_8);
                response.sendRedirect(errorUrl);
                return;
            }

            // Gọi service để xử lý user trong transaction và tạo JWT token
            String jwtToken = oAuth2UserService.processOAuth2UserAndGenerateToken(email, fullName, avatarUrl, authProvider);
            log.info("JWT token generated successfully for user: {}", email);

            // Redirect về frontend kèm token trong URL
            String redirectUrl = frontendRedirectUrl + "?token=" + jwtToken;
            log.info("Redirecting to frontend: {}", frontendRedirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing OAuth2 user: {}", e.getMessage(), e);
            String errorUrl = frontendRedirectUrl + "?error=" +
                    URLEncoder.encode("OAUTH2_LOGIN_FAILED", StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }
}
