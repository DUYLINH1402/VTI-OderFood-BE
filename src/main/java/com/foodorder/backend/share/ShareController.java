package com.foodorder.backend.share;

import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.share.dto.request.ShareRequest;
import com.foodorder.backend.share.dto.response.ShareResponse;
import com.foodorder.backend.share.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
@Tag(name = "Share API", description = "API quản lý lượt chia sẻ")
public class ShareController {

    private final ShareService shareService;

    /**
     * Ghi nhận lượt share
     * Không bắt buộc đăng nhập (cho phép khách vãng lai share)
     */
    @PostMapping
    @Operation(summary = "Ghi nhận lượt share", description = "Ghi nhận lượt chia sẻ lên mạng xã hội")
    public ResponseEntity<ShareResponse> recordShare(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShareRequest request
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        ShareResponse response = shareService.recordShare(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy số lượt share của một đối tượng
     */
    @GetMapping("/{targetType}/{targetId}/count")
    @Operation(summary = "Lấy số lượt share", description = "Lấy tổng số lượt share của một đối tượng")
    public ResponseEntity<Long> getShareCount(
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        long count = shareService.getShareCount(type, targetId);
        return ResponseEntity.ok(count);
    }
}

