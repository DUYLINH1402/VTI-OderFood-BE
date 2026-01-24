package com.foodorder.backend.like.repository;

import com.foodorder.backend.like.entity.Like;
import com.foodorder.backend.like.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * Tìm like của user cho một đối tượng cụ thể
     */
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);

    /**
     * Kiểm tra user đã like đối tượng chưa
     */
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);

    /**
     * Đếm số lượt like của một đối tượng
     */
    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * Lấy danh sách target_id đã được user like (theo loại)
     * Dùng để hiển thị trạng thái "đã like" trên danh sách món ăn
     */
    @Query("SELECT l.targetId FROM Like l WHERE l.user.id = :userId AND l.targetType = :targetType AND l.targetId IN :targetIds")
    List<Long> findLikedTargetIdsByUserAndType(
            @Param("userId") Long userId,
            @Param("targetType") TargetType targetType,
            @Param("targetIds") List<Long> targetIds
    );

    /**
     * Xóa like (unlike)
     */
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
}

