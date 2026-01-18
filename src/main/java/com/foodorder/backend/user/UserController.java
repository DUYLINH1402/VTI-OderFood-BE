package com.foodorder.backend.user;

import com.foodorder.backend.user.dto.request.ChangePasswordRequest;
import com.foodorder.backend.user.dto.request.UserUpdateRequest;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý thông tin người dùng
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API quản lý thông tin người dùng - Yêu cầu đăng nhập")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lấy thông tin cá nhân", description = "Lấy thông tin profile của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy userId từ CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Lấy lại User từ database với role được fetch sẵn thay vì dùng detached entity
        User user = userService.findUserWithRoleById(userId);

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cập nhật thông tin profile của người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PutMapping("/update-profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId(); // Lấy userId thay vì detached entity

        // Lấy user từ database với role được fetch sẵn
        User user = userService.findUserWithRoleById(userId);
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setAvatarUrl(request.getAvatarUrl());

        userService.save(user);

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload avatar", description = "Upload ảnh đại diện cho người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công - Trả về URL ảnh"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "500", description = "Lỗi upload")
    })
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "File ảnh cần upload") @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId(); // Đã đúng rồi

        String imageUrl = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ")
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId(); // Lấy userId thay vì detached entity
        
        userService.changePassword(userId, request);
        return ResponseEntity.ok("PASSWORD_CHANGED");
    }


}
