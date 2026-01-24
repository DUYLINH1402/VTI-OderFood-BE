package com.foodorder.backend.like.service.impl;

import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.like.dto.request.LikeRequest;
import com.foodorder.backend.like.dto.response.LikeResponse;
import com.foodorder.backend.like.entity.Like;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.like.repository.LikeRepository;
import com.foodorder.backend.like.service.LikeService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    /**
     * Toggle like: Nếu chưa like thì like, nếu đã like thì unlike
     * Đồng thời cập nhật totalLikes trong bảng đối tượng (Food, Blog...) một cách bất đồng bộ
     */
    @Override
    @Transactional
    public LikeResponse toggleLike(Long userId, LikeRequest request) {
        // Validate và parse targetType
        TargetType targetType = parseTargetType(request.getTargetType());
        Long targetId = request.getTargetId();

        // Validate user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Validate target tồn tại
        validateTargetExists(targetType, targetId);

        // Kiểm tra đã like chưa
        Optional<Like> existingLike = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        boolean isLiked;
        if (existingLike.isPresent()) {
            // Unlike: Xóa like hiện tại
            likeRepository.delete(existingLike.get());
            isLiked = false;
            // Cập nhật totalLikes giảm 1
            updateTotalLikesAsync(targetType, targetId, -1);
            log.info("User {} unliked {} with id {}", userId, targetType, targetId);
        } else {
            // Like: Tạo mới like
            Like newLike = Like.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            likeRepository.save(newLike);
            isLiked = true;
            // Cập nhật totalLikes tăng 1
            updateTotalLikesAsync(targetType, targetId, 1);
            log.info("User {} liked {} with id {}", userId, targetType, targetId);
        }

        // Lấy tổng số like hiện tại
        long totalLikes = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);

        return LikeResponse.builder()
                .liked(isLiked)
                .totalLikes(totalLikes)
                .targetType(targetType.name())
                .targetId(targetId)
                .build();
    }

    @Override
    public boolean isLiked(Long userId, TargetType targetType, Long targetId) {
        if (userId == null) {
            return false;
        }
        return likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    @Override
    public long getLikeCount(TargetType targetType, Long targetId) {
        return likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override
    public List<Long> getLikedTargetIds(Long userId, TargetType targetType, List<Long> targetIds) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return List.of();
        }
        return likeRepository.findLikedTargetIdsByUserAndType(userId, targetType, targetIds);
    }

    @Override
    public LikeResponse getLikeInfo(Long userId, TargetType targetType, Long targetId) {
        boolean liked = isLiked(userId, targetType, targetId);
        long totalLikes = getLikeCount(targetType, targetId);

        return LikeResponse.builder()
                .liked(liked)
                .totalLikes(totalLikes)
                .targetType(targetType.name())
                .targetId(targetId)
                .build();
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
            case MOVIE:
                // TODO: Sẽ implement khi có MovieRepository
                log.warn("Movie validation chưa được implement");
                break;
            default:
                throw new BadRequestException("Target type không được hỗ trợ: " + targetType, "UNSUPPORTED_TARGET_TYPE");
        }
    }

    /**
     * Cập nhật totalLikes vào bảng đối tượng một cách bất đồng bộ
     * Giúp tối ưu hiệu năng, tránh COUNT liên tục trong DB
     */
    @Async
    @Transactional
    public void updateTotalLikesAsync(TargetType targetType, Long targetId, int delta) {
        try {
            switch (targetType) {
                case FOOD:
                    foodRepository.findById(targetId).ifPresent(food -> {
                        Integer currentLikes = food.getTotalLikes() != null ? food.getTotalLikes() : 0;
                        food.setTotalLikes(Math.max(0, currentLikes + delta));
                        foodRepository.save(food);
                        log.debug("Updated totalLikes for Food {}: {}", targetId, food.getTotalLikes());
                    });
                    break;
                case BLOG:
                    // TODO: Implement khi có Blog entity
                    break;
                case MOVIE:
                    // TODO: Implement khi có Movie entity
                    break;
            }
        } catch (Exception e) {
            log.error("Error updating totalLikes for {} {}: {}", targetType, targetId, e.getMessage());
        }
    }
}

