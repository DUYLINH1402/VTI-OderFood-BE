package com.foodorder.backend.user.service;

import com.foodorder.backend.user.dto.request.AdminCreateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserStatusRequest;
import com.foodorder.backend.user.dto.response.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho admin quản lý user
 */
public interface AdminUserService {

    /**
     * Lấy danh sách tất cả users với phân trang và filter
     * @param keyword từ khóa tìm kiếm (username, email, fullName, phoneNumber)
     * @param roleCode filter theo role
     * @param isActive filter theo trạng thái active
     * @param pageable thông tin phân trang
     * @return Page<AdminUserResponse>
     */
    Page<AdminUserResponse> getAllUsers(String keyword, String roleCode, Boolean isActive, Pageable pageable);

    /**
     * Lấy danh sách users theo role với phân trang và filter
     * @param roleCode role code (ROLE_USER, ROLE_STAFF, ROLE_ADMIN)
     * @param keyword từ khóa tìm kiếm
     * @param isActive filter theo trạng thái active
     * @param pageable thông tin phân trang
     * @return Page<AdminUserResponse>
     */
    Page<AdminUserResponse> getUsersByRole(String roleCode, String keyword, Boolean isActive, Pageable pageable);

    /**
     * Lấy chi tiết user theo ID
     * @param userId ID của user
     * @return AdminUserResponse
     */
    AdminUserResponse getUserById(Long userId);

    /**
     * Tạo user mới
     * @param request thông tin user cần tạo
     * @return AdminUserResponse
     */
    AdminUserResponse createUser(AdminCreateUserRequest request);

    /**
     * Cập nhật thông tin user
     * @param userId ID của user
     * @param request thông tin cần cập nhật
     * @return AdminUserResponse
     */
    AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request);

    /**
     * Xóa user
     * @param userId ID của user cần xóa
     */
    void deleteUser(Long userId);

    /**
     * Thay đổi trạng thái user (khóa/mở khóa)
     * @param userId ID của user
     * @param request thông tin trạng thái mới
     * @return AdminUserResponse
     */
    AdminUserResponse updateUserStatus(Long userId, AdminUpdateUserStatusRequest request);

    /**
     * Admin gửi email reset mật khẩu cho user
     * @param userId ID của user cần reset mật khẩu
     */
    void sendResetPasswordEmail(Long userId);
}
