package com.foodorder.backend.comment.service;

import com.foodorder.backend.comment.dto.request.CreateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentStatusRequest;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.entity.CommentStatus;
import com.foodorder.backend.like.entity.TargetType;
import org.springframework.data.domain.Pageable;

/**
 * Service interface xử lý nghiệp vụ Comment
 */
public interface CommentService {

    /**
     * Tạo bình luận mới
     * @param userId ID của user thực hiện
     * @param request Thông tin bình luận
     * @return CommentResponse đã tạo
     */
    CommentResponse createComment(Long userId, CreateCommentRequest request);

    /**
     * Cập nhật nội dung bình luận
     * @param userId ID của user thực hiện
     * @param commentId ID của bình luận
     * @param request Nội dung mới
     * @return CommentResponse đã cập nhật
     */
    CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request);

    /**
     * Xóa bình luận (soft delete - chuyển trạng thái sang DELETED)
     * @param userId ID của user thực hiện
     * @param commentId ID của bình luận
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * Lấy danh sách bình luận của một đối tượng (phân trang)
     * Chỉ lấy các comment gốc (không có parent), có bao gồm replies
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse chứa danh sách comment
     */
    CommentPageResponse getCommentsByTarget(TargetType targetType, Long targetId, Pageable pageable);

    /**
     * Lấy chi tiết một bình luận
     * @param commentId ID của bình luận
     * @return CommentResponse
     */
    CommentResponse getCommentById(Long commentId);

    /**
     * Lấy các reply của một comment
     * @param parentId ID của comment cha
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse chứa danh sách reply
     */
    CommentPageResponse getReplies(Long parentId, Pageable pageable);

    /**
     * Đếm số bình luận (ACTIVE) của một đối tượng
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     * @return Số lượng bình luận
     */
    long countCommentsByTarget(TargetType targetType, Long targetId);

    /**
     * Lấy danh sách bình luận của user hiện tại
     * @param userId ID của user
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse
     */
    CommentPageResponse getMyComments(Long userId, Pageable pageable);

    // ========== ADMIN METHODS ==========

    /**
     * [ADMIN] Thay đổi trạng thái bình luận (ẩn/hiện/xóa)
     * @param commentId ID của bình luận
     * @param request Trạng thái mới
     * @return CommentResponse đã cập nhật
     */
    CommentResponse updateCommentStatus(Long commentId, UpdateCommentStatusRequest request);

    /**
     * [ADMIN] Lấy tất cả bình luận (phân trang)
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse
     */
    CommentPageResponse getAllComments(Pageable pageable);

    /**
     * [ADMIN] Lấy bình luận theo trạng thái
     * @param status Trạng thái cần lọc
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse
     */
    CommentPageResponse getCommentsByStatus(CommentStatus status, Pageable pageable);

    /**
     * [ADMIN] Tìm kiếm bình luận theo nội dung
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return CommentPageResponse
     */
    CommentPageResponse searchComments(String keyword, Pageable pageable);

    /**
     * [ADMIN] Xóa vĩnh viễn một bình luận (hard delete)
     * @param commentId ID của bình luận
     */
    void hardDeleteComment(Long commentId);
}

