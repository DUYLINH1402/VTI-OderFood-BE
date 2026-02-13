package com.foodorder.backend.comment.service.impl;

import com.foodorder.backend.comment.dto.request.BatchDeleteRequest;
import com.foodorder.backend.comment.dto.request.BatchUpdateStatusRequest;
import com.foodorder.backend.comment.dto.request.CreateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentStatusRequest;
import com.foodorder.backend.comment.dto.response.BatchOperationResponse;
import com.foodorder.backend.comment.dto.response.CommentNotification;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.dto.response.CommentStatisticsResponse;
import com.foodorder.backend.comment.entity.Comment;
import com.foodorder.backend.comment.entity.CommentStatus;
import com.foodorder.backend.comment.repository.CommentRepository;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của CommentService
 * Xử lý các nghiệp vụ liên quan đến bình luận
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Parse target type
        TargetType targetType;
        try {
            targetType = TargetType.valueOf(request.getTargetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Loại đối tượng không hợp lệ", "INVALID_TARGET_TYPE");
        }

        // Validate target exists
        validateTargetExists(targetType, request.getTargetId());

        // Build comment entity
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .user(user)
                .content(request.getContent())
                .targetType(targetType)
                .targetId(request.getTargetId())
                .status(CommentStatus.ACTIVE);

        // Nếu là reply, validate parent comment tồn tại
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận cha", "PARENT_COMMENT_NOT_FOUND"));

            // Validate parent comment cùng target
            if (!parentComment.getTargetType().equals(targetType) || !parentComment.getTargetId().equals(request.getTargetId())) {
                throw new BadRequestException("Reply phải cùng đối tượng với comment cha", "INVALID_PARENT_TARGET");
            }

            // Không cho phép reply của reply (chỉ 2 cấp)
            if (parentComment.getParent() != null) {
                throw new BadRequestException("Không thể reply cho một reply", "NESTED_REPLY_NOT_ALLOWED");
            }

            commentBuilder.parent(parentComment);
        }

        Comment comment = commentBuilder.build();
        comment = commentRepository.save(comment);

        log.info("Tạo bình luận mới: userId={}, targetType={}, targetId={}, commentId={}",
                userId, targetType, request.getTargetId(), comment.getId());

        CommentResponse response = CommentResponse.fromEntity(comment);

        // Gửi WebSocket notification cho các client đang xem đối tượng này
        sendCommentNotification(CommentNotification.newComment(
                response,
                countCommentsByTarget(targetType, request.getTargetId())
        ), targetType, request.getTargetId());

        return response;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request) {
        // Tìm comment và validate quyền sở hữu
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bình luận hoặc bạn không có quyền chỉnh sửa", "COMMENT_NOT_FOUND_OR_UNAUTHORIZED"));

        // Không cho phép sửa comment đã bị xóa
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new BadRequestException("Không thể chỉnh sửa bình luận đã bị xóa", "COMMENT_DELETED");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        log.info("Cập nhật bình luận: commentId={}, userId={}", commentId, userId);

        CommentResponse response = CommentResponse.fromEntity(comment);

        // Gửi WebSocket notification
        sendCommentNotification(
                CommentNotification.commentUpdated(response),
                comment.getTargetType(),
                comment.getTargetId()
        );

        return response;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public void deleteComment(Long userId, Long commentId) {
        // Tìm comment và validate quyền sở hữu
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bình luận hoặc bạn không có quyền xóa", "COMMENT_NOT_FOUND_OR_UNAUTHORIZED"));

        TargetType targetType = comment.getTargetType();
        Long targetId = comment.getTargetId();
        Long commentIdDeleted = comment.getId();

        // Soft delete - chuyển trạng thái sang DELETED
        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);

        log.info("Xóa bình luận (soft): commentId={}, userId={}", commentId, userId);

        // Gửi WebSocket notification
        sendCommentNotification(
                CommentNotification.commentDeleted(
                        targetType.name(),
                        targetId,
                        commentIdDeleted,
                        countCommentsByTarget(targetType, targetId)
                ),
                targetType,
                targetId
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COMMENTS_BY_TARGET_CACHE,
               key = "#targetType.name() + '_' + #targetId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public CommentPageResponse getCommentsByTarget(TargetType targetType, Long targetId, Pageable pageable) {
        // Validate target exists
        validateTargetExists(targetType, targetId);

        // Lấy danh sách comment gốc (có status ACTIVE)
        Page<Comment> commentPage = commentRepository.findRootCommentsByTarget(
                targetType, targetId, CommentStatus.ACTIVE, pageable);

        // Convert to response với replies
        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> {
                    // Lấy replies cho mỗi comment
                    List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId(), CommentStatus.ACTIVE);
                    comment.setReplies(replies);
                    return CommentResponse.fromEntity(comment, true);
                })
                .collect(Collectors.toList());

        // Đếm tổng số comment (bao gồm cả replies)
        long totalComments = commentRepository.countByTargetTypeAndTargetIdAndStatus(targetType, targetId, CommentStatus.ACTIVE);

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(totalComments)
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận", "COMMENT_NOT_FOUND"));

        // Lấy replies nếu là comment gốc
        if (comment.getParent() == null) {
            List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId(), CommentStatus.ACTIVE);
            comment.setReplies(replies);
            return CommentResponse.fromEntity(comment, true);
        }

        return CommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResponse getReplies(Long parentId, Pageable pageable) {
        // Validate parent comment exists
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận cha", "PARENT_COMMENT_NOT_FOUND"));

        // Lấy replies
        List<Comment> replies = commentRepository.findRepliesByParentId(parentId, CommentStatus.ACTIVE);

        List<CommentResponse> replyResponses = replies.stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        long replyCount = commentRepository.countByParentIdAndStatus(parentId, CommentStatus.ACTIVE);

        return CommentPageResponse.builder()
                .comments(replyResponses)
                .totalComments(replyCount)
                .totalPages(1)
                .currentPage(0)
                .pageSize(replies.size())
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COMMENT_COUNT_CACHE, key = "#targetType.name() + '_' + #targetId")
    public long countCommentsByTarget(TargetType targetType, Long targetId) {
        return commentRepository.countByTargetTypeAndTargetIdAndStatus(targetType, targetId, CommentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResponse getMyComments(Long userId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId, CommentStatus.ACTIVE, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    // ========== ADMIN METHODS ==========

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public CommentResponse updateCommentStatus(Long commentId, UpdateCommentStatusRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận", "COMMENT_NOT_FOUND"));

        CommentStatus oldStatus = comment.getStatus();
        comment.setStatus(request.getStatus());
        comment = commentRepository.save(comment);

        log.info("[ADMIN] Cập nhật trạng thái bình luận: commentId={}, oldStatus={}, newStatus={}",
                commentId, oldStatus, request.getStatus());

        return CommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_COMMENTS_CACHE, key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public CommentPageResponse getAllComments(Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findAllOrderByCreatedAtDesc(pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_COMMENTS_CACHE, key = "'status_' + #status.name() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public CommentPageResponse getCommentsByStatus(CommentStatus status, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResponse searchComments(String keyword, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.searchByContent(keyword, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public void hardDeleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận", "COMMENT_NOT_FOUND"));

        TargetType targetType = comment.getTargetType();
        Long targetId = comment.getTargetId();

        commentRepository.delete(comment);

        log.info("[ADMIN] Xóa vĩnh viễn bình luận: commentId={}", commentId);

        // Gửi WebSocket notification
        sendCommentNotification(
                CommentNotification.commentDeleted(
                        targetType.name(),
                        targetId,
                        commentId,
                        countCommentsByTarget(targetType, targetId)
                ),
                targetType,
                targetId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResponse getCommentsByUser(Long userId, Pageable pageable) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND");
        }

        Page<Comment> commentPage = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPageResponse getCommentsByTargetForAdmin(TargetType targetType, Long targetId, Pageable pageable) {
        Page<Comment> commentPage = commentRepository.findByTargetTypeAndTargetId(targetType, targetId, pageable);

        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        // Đếm tổng số comment (bao gồm tất cả trạng thái)
        long totalComments = commentRepository.countByTargetTypeAndTargetId(targetType, targetId);

        return CommentPageResponse.builder()
                .comments(comments)
                .totalComments(totalComments)
                .totalPages(commentPage.getTotalPages())
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_COMMENTS_CACHE, key = "'statistics'")
    public CommentStatisticsResponse getCommentStatistics() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        java.time.LocalDateTime sevenDaysAgo = now.minusDays(7);
        java.time.LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalComments = commentRepository.count();
        long activeComments = commentRepository.countByStatus(CommentStatus.ACTIVE);
        long hiddenComments = commentRepository.countByStatus(CommentStatus.HIDDEN);
        long deletedComments = commentRepository.countByStatus(CommentStatus.DELETED);
        long commentsToday = commentRepository.countCommentsFromDate(startOfToday);
        long commentsLast7Days = commentRepository.countCommentsFromDate(sevenDaysAgo);
        long commentsLast30Days = commentRepository.countCommentsFromDate(thirtyDaysAgo);

        log.info("[ADMIN] Lấy thống kê bình luận: total={}, active={}, hidden={}, deleted={}",
                totalComments, activeComments, hiddenComments, deletedComments);

        return CommentStatisticsResponse.builder()
                .totalComments(totalComments)
                .activeComments(activeComments)
                .hiddenComments(hiddenComments)
                .deletedComments(deletedComments)
                .commentsToday(commentsToday)
                .commentsLast7Days(commentsLast7Days)
                .commentsLast30Days(commentsLast30Days)
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public BatchOperationResponse batchUpdateStatus(BatchUpdateStatusRequest request) {
        List<Long> commentIds = request.getCommentIds();
        CommentStatus newStatus = request.getStatus();

        List<Comment> comments = commentRepository.findByIdIn(commentIds);
        List<Long> foundIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Long> failedIds = commentIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        // Cập nhật trạng thái cho các comment tìm thấy
        for (Comment comment : comments) {
            comment.setStatus(newStatus);
        }
        commentRepository.saveAll(comments);

        int successCount = comments.size();
        int failCount = failedIds.size();

        log.info("[ADMIN] Batch update status: successCount={}, failCount={}, newStatus={}",
                successCount, failCount, newStatus);

        return BatchOperationResponse.builder()
                .successCount(successCount)
                .failCount(failCount)
                .failedIds(failedIds)
                .message(String.format("Đã cập nhật %d bình luận thành trạng thái %s", successCount, newStatus))
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.COMMENTS_BY_TARGET_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.COMMENT_COUNT_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_COMMENTS_CACHE, allEntries = true)
    })
    public BatchOperationResponse batchHardDelete(BatchDeleteRequest request) {
        List<Long> commentIds = request.getCommentIds();

        List<Comment> comments = commentRepository.findByIdIn(commentIds);
        List<Long> foundIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Long> failedIds = commentIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        // Xóa vĩnh viễn các comment tìm thấy
        commentRepository.deleteAll(comments);

        int successCount = comments.size();
        int failCount = failedIds.size();

        log.info("[ADMIN] Batch hard delete: successCount={}, failCount={}", successCount, failCount);

        return BatchOperationResponse.builder()
                .successCount(successCount)
                .failCount(failCount)
                .failedIds(failedIds)
                .message(String.format("Đã xóa vĩnh viễn %d bình luận", successCount))
                .build();
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Validate đối tượng được bình luận có tồn tại không
     */
    private void validateTargetExists(TargetType targetType, Long targetId) {
        switch (targetType) {
            case FOOD:
                if (!foodRepository.existsById(targetId)) {
                    throw new ResourceNotFoundException("Không tìm thấy món ăn", "FOOD_NOT_FOUND");
                }
                break;
            case BLOG:
                // TODO: Validate blog exists khi có BlogRepository
                log.debug("Bỏ qua validate Blog (chưa có BlogRepository)");
                break;
            case MOVIE:
                // TODO: Validate movie exists khi có MovieRepository
                log.debug("Bỏ qua validate Movie (chưa có MovieRepository)");
                break;
            default:
                throw new BadRequestException("Loại đối tượng không được hỗ trợ", "UNSUPPORTED_TARGET_TYPE");
        }
    }

    /**
     * Gửi thông báo WebSocket cho các client đang theo dõi comment của một đối tượng
     * @param notification Thông báo cần gửi
     * @param targetType Loại đối tượng
     * @param targetId ID đối tượng
     */
    private void sendCommentNotification(CommentNotification notification, TargetType targetType, Long targetId) {
        try {
            // Gửi đến topic chung cho đối tượng (VD: /topic/comments/FOOD/123)
            String destination = String.format("/topic/comments/%s/%d", targetType.name(), targetId);
            messagingTemplate.convertAndSend(destination, notification);
            log.debug("Đã gửi WebSocket notification: destination={}, eventType={}",
                    destination, notification.getEventType());
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến luồng chính
            log.error("Lỗi khi gửi WebSocket notification: {}", e.getMessage());
        }
    }
}

