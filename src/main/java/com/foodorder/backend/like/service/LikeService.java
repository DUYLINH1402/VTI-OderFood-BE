package com.foodorder.backend.like.service;

import com.foodorder.backend.like.dto.request.LikeRequest;
import com.foodorder.backend.like.dto.response.LikeResponse;
import com.foodorder.backend.like.entity.TargetType;

import java.util.List;
import java.util.Map;

/**
 * Service xử lý nghiệp vụ Like
 */
public interface LikeService {

    /**
     * Toggle like (like nếu chưa like, unlike nếu đã like)
     * @param userId ID của user thực hiện
     * @param request Thông tin đối tượng cần like
     * @return LikeResponse chứa trạng thái like và tổng số lượt like
     */
    LikeResponse toggleLike(Long userId, LikeRequest request);

    /**
     * Kiểm tra user đã like đối tượng chưa
     * @param userId ID của user
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @return true nếu đã like, false nếu chưa
     */
    boolean isLiked(Long userId, TargetType targetType, Long targetId);

    /**
     * Lấy tổng số lượt like của một đối tượng
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @return Số lượt like
     */
    long getLikeCount(TargetType targetType, Long targetId);

    /**
     * Lấy danh sách các ID đã được user like (dùng để hiển thị trạng thái trên danh sách)
     * @param userId ID của user
     * @param targetType Loại đối tượng
     * @param targetIds Danh sách ID cần kiểm tra
     * @return Danh sách ID đã được like
     */
    List<Long> getLikedTargetIds(Long userId, TargetType targetType, List<Long> targetIds);

    /**
     * Lấy thông tin like cho một đối tượng
     * @param userId ID user (có thể null nếu chưa đăng nhập)
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @return LikeResponse chứa trạng thái và số lượt like
     */
    LikeResponse getLikeInfo(Long userId, TargetType targetType, Long targetId);
}

