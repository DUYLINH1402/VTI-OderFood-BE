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
 * Controller cho admin quản lý nhân viên
 * Base URL: /api/admin/employees
 * Nhân viên là user có role ROLE_STAFF
 */
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin Employees", description = "API quản lý nhân viên dành cho Admin")
public class AdminEmployeeController {

    private static final String EMPLOYEE_ROLE_CODE = "ROLE_STAFF";

    private final AdminUserService adminUserService;

    @Operation(summary = "Danh sách nhân viên", description = "Lấy danh sách nhân viên với phân trang và filter.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllEmployees(
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

        // Lọc theo role ROLE_STAFF
        Page<AdminUserResponse> employees = adminUserService.getUsersByRole(EMPLOYEE_ROLE_CODE, keyword, isActive, pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Chi tiết nhân viên", description = "Xem chi tiết thông tin một nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getEmployeeById(
            @Parameter(description = "ID nhân viên") @PathVariable Long id) {
        AdminUserResponse employee = adminUserService.getUserById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Thêm nhân viên", description = "Tạo mới một nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createEmployee(@Valid @RequestBody AdminCreateUserRequest request) {

        // Luôn set role là ROLE_STAFF cho nhân viên
        request.setRoleCode(EMPLOYEE_ROLE_CODE);

        AdminUserResponse employee = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @Operation(summary = "Cập nhật nhân viên", description = "Cập nhật thông tin nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        // Không cho phép thay đổi role thông qua API employees
        request.setRoleCode(null);

        AdminUserResponse employee = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Xóa nhân viên", description = "Xóa một nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long id) {

        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Thay đổi trạng thái", description = "Khóa/mở khóa tài khoản nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateEmployeeStatus(
            @Parameter(description = "ID nhân viên") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse employee = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Gửi email reset mật khẩu", description = "Gửi email để nhân viên đặt lại mật khẩu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi email thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "ID nhân viên") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}
