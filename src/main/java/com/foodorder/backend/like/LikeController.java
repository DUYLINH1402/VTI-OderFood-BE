package com.foodorder.backend.like;

import com.foodorder.backend.like.dto.request.LikeRequest;
import com.foodorder.backend.like.dto.response.LikeResponse;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.like.service.LikeService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Tag(name = "Like API", description = "API quản lý lượt thích")
public class LikeController {

    private final LikeService likeService;

    /**
     * Toggle like/unlike cho một đối tượng
     * Yêu cầu đăng nhập
     */
    @PostMapping("/toggle")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Toggle like/unlike", description = "Like nếu chưa like, unlike nếu đã like")
    public ResponseEntity<LikeResponse> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LikeRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        LikeResponse response = likeService.toggleLike(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin like của một đối tượng
     * Không yêu cầu đăng nhập, nhưng nếu đã đăng nhập sẽ trả về trạng thái "đã like" của user
     */
    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "Lấy thông tin like", description = "Lấy số lượt like và trạng thái đã like của user")
    public ResponseEntity<LikeResponse> getLikeInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        LikeResponse response = likeService.getLikeInfo(userId, type, targetId);
        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra user đã like chưa
     * Yêu cầu đăng nhập
     */
    @GetMapping("/check/{targetType}/{targetId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kiểm tra đã like", description = "Kiểm tra user hiện tại đã like đối tượng chưa")
    public ResponseEntity<Boolean> checkLiked(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        Long userId = userDetails.getUser().getId();
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        boolean isLiked = likeService.isLiked(userId, type, targetId);
        return ResponseEntity.ok(isLiked);
    }
}

