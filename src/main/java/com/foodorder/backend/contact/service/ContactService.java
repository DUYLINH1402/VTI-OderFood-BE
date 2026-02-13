package com.foodorder.backend.contact.service;

import com.foodorder.backend.contact.dto.*;
import com.foodorder.backend.contact.entity.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface cho quản lý tin nhắn liên hệ
 */
public interface ContactService {

    /**
     * Gửi tin nhắn liên hệ từ khách hàng (có rate limiting)
     * @param request Thông tin tin nhắn
     * @param ipAddress IP của người gửi
     * @return Response chứa thông tin tin nhắn đã lưu
     */
    ContactResponse submitContact(ContactRequest request, String ipAddress);

    /**
     * Lấy danh sách tất cả tin nhắn (Admin)
     */
    Page<ContactResponse> getAllContacts(Pageable pageable);

    /**
     * Lấy danh sách tin nhắn theo trạng thái (Admin)
     */
    Page<ContactResponse> getContactsByStatus(ContactStatus status, Pageable pageable);

    /**
     * Lấy danh sách tin nhắn theo nhiều trạng thái (Admin)
     */
    Page<ContactResponse> getContactsByStatuses(List<ContactStatus> statuses, Pageable pageable);

    /**
     * Tìm kiếm tin nhắn theo keyword (Admin)
     */
    Page<ContactResponse> searchContacts(String keyword, Pageable pageable);

    /**
     * Lấy chi tiết một tin nhắn (Admin)
     */
    ContactResponse getContactById(Long id);

    /**
     * Cập nhật trạng thái tin nhắn (Admin)
     */
    ContactResponse updateContactStatus(Long id, ContactUpdateRequest request);

    /**
     * Phản hồi tin nhắn liên hệ (Admin)
     * @param id ID tin nhắn
     * @param request Nội dung phản hồi
     * @param adminId ID admin phản hồi
     * @return Response chứa thông tin tin nhắn đã cập nhật
     */
    ContactResponse replyToContact(Long id, ContactReplyRequest request, Long adminId);

    /**
     * Xóa tin nhắn (Admin - chỉ xóa tin đã archived)
     */
    void deleteContact(Long id);

    /**
     * Đếm số tin nhắn chưa đọc (Admin)
     */
    long countPendingMessages();

    /**
     * Thống kê tin nhắn (Admin)
     */
    Map<String, Object> getContactStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy danh sách tin nhắn mới nhất (Admin Dashboard)
     */
    List<ContactResponse> getRecentContacts(int limit);
}

