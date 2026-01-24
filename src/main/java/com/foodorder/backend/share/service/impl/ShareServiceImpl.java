package com.foodorder.backend.share.service.impl;

import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.share.dto.request.ShareRequest;
import com.foodorder.backend.share.dto.response.ShareResponse;
import com.foodorder.backend.share.entity.Share;
import com.foodorder.backend.share.entity.SharePlatform;
import com.foodorder.backend.share.repository.ShareRepository;
import com.foodorder.backend.share.service.ShareService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareServiceImpl implements ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    /**
     * Ghi nhận lượt share vào database
     * Cho phép khách vãng lai share (userId = null)
     * Đồng thời cập nhật totalShares trong bảng đối tượng một cách bất đồng bộ
     */
    @Override
    @Transactional
    public ShareResponse recordShare(Long userId, ShareRequest request) {
        // Parse và validate
        TargetType targetType = parseTargetType(request.getTargetType());
        SharePlatform platform = parsePlatform(request.getPlatform());
        Long targetId = request.getTargetId();

        // Validate target tồn tại
        validateTargetExists(targetType, targetId);

        // Lấy user nếu có
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // Tạo bản ghi share
        Share share = Share.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .platform(platform)
                .build();
        shareRepository.save(share);

        // Cập nhật totalShares bất đồng bộ
        updateTotalSharesAsync(targetType, targetId, 1);

        log.info("Recorded share for {} {} on {} by user {}", targetType, targetId, platform, userId);

        // Lấy tổng số share
        long totalShares = shareRepository.countByTargetTypeAndTargetId(targetType, targetId);

        return ShareResponse.builder()
                .totalShares(totalShares)
                .targetType(targetType.name())
                .targetId(targetId)
                .platform(platform.name())
                .message("Chia sẻ thành công")
                .build();
    }

    @Override
    public long getShareCount(TargetType targetType, Long targetId) {
        return shareRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * Parse và validate targetType từ String
     */
    private TargetType parseTargetType(String targetTypeStr) {
        try {
            return TargetType.valueOf(targetTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Target type không hợp lệ: " + targetTypeStr, "INVALID_TARGET_TYPE");
        }
    }

    /**
     * Parse và validate platform từ String
     */
    private SharePlatform parsePlatform(String platformStr) {
        try {
            return SharePlatform.valueOf(platformStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Platform không hợp lệ: " + platformStr, "INVALID_PLATFORM");
        }
    }

    /**
     * Validate đối tượng target có tồn tại không
     */
    private void validateTargetExists(TargetType targetType, Long targetId) {
        switch (targetType) {
            case FOOD:
                if (!foodRepository.existsById(targetId)) {
                    throw new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + targetId, "FOOD_NOT_FOUND");
                }
                break;
            case BLOG:
                // TODO: Sẽ implement khi có BlogRepository
                log.warn("Blog validation chưa được implement");
                break;
            default:
                throw new BadRequestException("Target type không được hỗ trợ cho share: " + targetType, "UNSUPPORTED_TARGET_TYPE");
        }
    }

    /**
     * Cập nhật totalShares vào bảng đối tượng một cách bất đồng bộ
     * Giúp tối ưu hiệu năng, tránh COUNT liên tục trong DB
     */
    @Async
    @Transactional
    public void updateTotalSharesAsync(TargetType targetType, Long targetId, int delta) {
        try {
            switch (targetType) {
                case FOOD:
                    foodRepository.findById(targetId).ifPresent(food -> {
                        Integer currentShares = food.getTotalShares() != null ? food.getTotalShares() : 0;
                        food.setTotalShares(Math.max(0, currentShares + delta));
                        foodRepository.save(food);
                        log.debug("Updated totalShares for Food {}: {}", targetId, food.getTotalShares());
                    });
                    break;
                case BLOG:
                    // TODO: Implement khi có Blog entity
                    break;
            }
        } catch (Exception e) {
            log.error("Error updating totalShares for {} {}: {}", targetType, targetId, e.getMessage());
        }
    }
}

