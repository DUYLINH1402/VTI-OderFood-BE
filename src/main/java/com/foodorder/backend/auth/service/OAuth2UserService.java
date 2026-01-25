package com.foodorder.backend.auth.service;

/**
 * Service xử lý OAuth2 User trong transaction
 * Hỗ trợ nhiều provider: Google, Facebook
 * Giải quyết vấn đề LazyInitializationException khi truy cập Role entity
 */
public interface OAuth2UserService {

    /**
     * Xử lý OAuth2 user: tìm hoặc tạo mới user, và tạo JWT token
     * Method này chạy trong transaction để đảm bảo lazy loading hoạt động
     *
     * @param email Email từ OAuth2 provider
     * @param fullName Tên đầy đủ từ provider
     * @param avatarUrl URL avatar từ provider
     * @param authProvider Provider xác thực (GOOGLE, FACEBOOK)
     * @return JWT token
     */
    String processOAuth2UserAndGenerateToken(String email, String fullName, String avatarUrl, String authProvider);
}

