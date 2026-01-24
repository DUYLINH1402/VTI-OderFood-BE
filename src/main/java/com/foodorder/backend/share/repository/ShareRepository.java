package com.foodorder.backend.share.repository;

import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.share.entity.Share;
import com.foodorder.backend.share.entity.SharePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    /**
     * Đếm số lượt share của một đối tượng
     */
    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * Đếm số lượt share của một đối tượng theo nền tảng cụ thể
     */
    long countByTargetTypeAndTargetIdAndPlatform(TargetType targetType, Long targetId, SharePlatform platform);
}

