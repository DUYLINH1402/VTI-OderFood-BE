package com.foodorder.backend.user.controller;

import com.foodorder.backend.user.dto.request.AdminCreateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserStatusRequest;
import com.foodorder.backend.user.dto.response.AdminUserResponse;
import com.foodorder.backend.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho admin quản lý người dùng (khách hàng)
 * Base URL: /api/admin/users
 * Chỉ quản lý user có role ROLE_USER
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin Users", description = "API quản lý khách hàng dành cho Admin")
public class AdminUserController {

    private static final String USER_ROLE_CODE = "ROLE_USER";

    private final AdminUserService adminUserService;

    @Operation(summary = "Danh sách khách hàng", description = "Lấy danh sách khách hàng với phân trang và filter.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Chỉ lấy user có role ROLE_USER (khách hàng)
        Page<AdminUserResponse> users = adminUserService.getUsersByRole(USER_ROLE_CODE, keyword, isActive, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Chi tiết khách hàng", description = "Xem chi tiết thông tin một khách hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @Parameter(description = "ID khách hàng") @PathVariable Long id) {

        AdminUserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Thêm khách hàng", description = "Tạo mới một khách hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {

        // Luôn set role là ROLE_USER cho khách hàng
        request.setRoleCode(USER_ROLE_CODE);

        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Cập nhật khách hàng", description = "Cập nhật thông tin khách hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @Parameter(description = "ID khách hàng") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        // Không cho phép thay đổi role thông qua API users
        request.setRoleCode(null);

        AdminUserResponse user = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Xóa khách hàng", description = "Xóa một khách hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID khách hàng") @PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Thay đổi trạng thái", description = "Khóa/mở khóa tài khoản khách hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @Parameter(description = "ID khách hàng") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Gửi email reset mật khẩu", description = "Gửi email để khách hàng đặt lại mật khẩu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi email thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "ID khách hàng") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}
