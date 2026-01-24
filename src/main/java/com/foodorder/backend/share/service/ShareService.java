package com.foodorder.backend.share.service;

import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.share.dto.request.ShareRequest;
import com.foodorder.backend.share.dto.response.ShareResponse;

/**
 * Service xử lý nghiệp vụ Share
 */
public interface ShareService {

    /**
     * Ghi nhận lượt share
     * @param userId ID user (có thể null nếu là khách vãng lai)
     * @param request Thông tin share
     * @return ShareResponse chứa thông tin kết quả
     */
    ShareResponse recordShare(Long userId, ShareRequest request);

    /**
     * Lấy tổng số lượt share của một đối tượng
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @return Số lượt share
     */
    long getShareCount(TargetType targetType, Long targetId);
}

