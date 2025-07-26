package com.foodorder.backend.user;

import com.foodorder.backend.user.dto.request.ChangePasswordRequest;
import com.foodorder.backend.user.dto.request.UserUpdateRequest;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Quan trọng: ép kiểu chính xác về CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setAvatarUrl(request.getAvatarUrl());

        userService.save(user);

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getUser().getId();

        String imageUrl = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        userService.changePassword(userDetails.getUser().getId(), request);
        return ResponseEntity.ok("PASSWORD_CHANGED");
    }


}
