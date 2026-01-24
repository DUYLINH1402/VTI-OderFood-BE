package com.foodorder.backend.comment.repository;

import com.foodorder.backend.comment.entity.Comment;
import com.foodorder.backend.comment.entity.CommentStatus;
import com.foodorder.backend.like.entity.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý truy vấn dữ liệu cho Comment
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Lấy danh sách bình luận gốc (không có parent) của một đối tượng
     * Chỉ lấy các comment có trạng thái ACTIVE
     */
    @Query("SELECT c FROM Comment c WHERE c.targetType = :targetType AND c.targetId = :targetId " +
           "AND c.parent IS NULL AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findRootCommentsByTarget(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            @Param("status") CommentStatus status,
            Pageable pageable
    );

    /**
     * Lấy tất cả bình luận gốc của một đối tượng (cho admin)
     */
    @Query("SELECT c FROM Comment c WHERE c.targetType = :targetType AND c.targetId = :targetId " +
           "AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findAllRootCommentsByTarget(
            @Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId,
            Pageable pageable
    );

    /**
     * Lấy các reply của một comment
     */
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.status = :status ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(
            @Param("parentId") Long parentId,
            @Param("status") CommentStatus status
    );

    /**
     * Đếm số bình luận (ACTIVE) của một đối tượng
     */
    long countByTargetTypeAndTargetIdAndStatus(TargetType targetType, Long targetId, CommentStatus status);

    /**
     * Đếm tổng số bình luận của một đối tượng (bao gồm cả hidden)
     */
    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    /**
     * Lấy danh sách bình luận của một user
     */
    Page<Comment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, CommentStatus status, Pageable pageable);

    /**
     * Tìm comment theo ID và user (để kiểm tra quyền sở hữu)
     */
    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    /**
     * Đếm số reply của một comment
     */
    long countByParentIdAndStatus(Long parentId, CommentStatus status);

    /**
     * Lấy tất cả comment (cho admin quản lý)
     */
    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
    Page<Comment> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Tìm comment theo status (cho admin)
     */
    Page<Comment> findByStatusOrderByCreatedAtDesc(CommentStatus status, Pageable pageable);

    /**
     * Tìm kiếm comment theo nội dung (cho admin)
     */
    @Query("SELECT c FROM Comment c WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.createdAt DESC")
    Page<Comment> searchByContent(@Param("keyword") String keyword, Pageable pageable);
}

