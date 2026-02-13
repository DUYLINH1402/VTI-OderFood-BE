package com.foodorder.backend.contact.service.impl;

import com.foodorder.backend.contact.dto.*;
import com.foodorder.backend.contact.entity.ContactMessage;
import com.foodorder.backend.contact.entity.ContactStatus;
import com.foodorder.backend.contact.repository.ContactMessageRepository;
import com.foodorder.backend.contact.service.ContactService;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.exception.TooManyRequestException;
import com.foodorder.backend.service.BrevoEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Triển khai ContactService - xử lý logic nghiệp vụ cho tin nhắn liên hệ
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final BrevoEmailService brevoEmailService;

    /**
     * Giới hạn số tin nhắn tối đa từ một IP trong 1 phút
     */
    private static final int MAX_MESSAGES_PER_MINUTE_PER_IP = 3;

    /**
     * Giới hạn số tin nhắn tối đa từ một email trong 1 giờ
     */
    private static final int MAX_MESSAGES_PER_HOUR_PER_EMAIL = 5;

    /**
     * Email admin nhận thông báo tin nhắn mới
     */
    @Value("${app.admin.email:admin@dongxanhfood.com}")
    private String adminEmail;

    /**
     * Tên cửa hàng
     */
    @Value("${app.store.name:Dong Xanh Food}")
    private String storeName;

    /**
     * Gửi tin nhắn liên hệ từ khách hàng
     * Bước 1: Kiểm tra rate limiting (chống spam)
     * Bước 2: Lưu tin nhắn vào DB ngay lập tức
     * Bước 3: Gửi thông báo cho admin (async - không bắt khách đợi)
     */
    @Override
    public ContactResponse submitContact(ContactRequest request, String ipAddress) {
        // Bước 1: Rate Limiting - Kiểm tra spam
        checkRateLimiting(request.getEmail(), ipAddress);

        // Bước 2: Lưu tin nhắn vào DB ngay lập tức
        ContactMessage contactMessage = ContactMessage.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .subject(request.getSubject() != null ? request.getSubject().trim() : null)
                .message(request.getMessage().trim())
                .status(ContactStatus.PENDING)
                .ipAddress(ipAddress)
                .notificationSent(false)
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
        log.info("Đã lưu tin nhắn liên hệ mới từ: {} (ID: {})", request.getEmail(), savedMessage.getId());

        // Bước 3: Gửi thông báo cho admin (async - chạy ngầm)
        sendNotificationToAdminAsync(savedMessage);

        return mapToResponse(savedMessage);
    }

    /**
     * Kiểm tra rate limiting để chống spam
     * - Tối đa 3 tin nhắn/phút từ cùng 1 IP
     * - Tối đa 5 tin nhắn/giờ từ cùng 1 email
     */
    private void checkRateLimiting(String email, String ipAddress) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Kiểm tra số tin nhắn từ IP trong 1 phút
        long countByIp = contactMessageRepository.countByIpAddressSince(ipAddress, oneMinuteAgo);
        if (countByIp >= MAX_MESSAGES_PER_MINUTE_PER_IP) {
            log.warn("Rate limit exceeded for IP: {} ({} messages in 1 minute)", ipAddress, countByIp);
            throw new TooManyRequestException("Bạn đã gửi quá nhiều tin nhắn. Vui lòng thử lại sau 1 phút.", "CONTACT_RATE_LIMIT_IP");
        }

        // Kiểm tra số tin nhắn từ email trong 1 giờ
        long countByEmail = contactMessageRepository.countByEmailSince(email.trim().toLowerCase(), oneHourAgo);
        if (countByEmail >= MAX_MESSAGES_PER_HOUR_PER_EMAIL) {
            log.warn("Rate limit exceeded for email: {} ({} messages in 1 hour)", email, countByEmail);
            throw new TooManyRequestException("Bạn đã gửi quá nhiều tin nhắn từ email này. Vui lòng thử lại sau.", "CONTACT_RATE_LIMIT_EMAIL");
        }
    }

    /**
     * Gửi thông báo cho admin về tin nhắn mới (Async - chạy ngầm)
     * Không làm ảnh hưởng tới trải nghiệm của khách hàng
     */
    @Async("taskExecutor")
    public void sendNotificationToAdminAsync(ContactMessage message) {
        try {
            String subject = String.format("[%s] Tin nhắn liên hệ mới từ %s", storeName, message.getName());
            String htmlContent = buildAdminNotificationEmail(message);

            brevoEmailService.sendEmail(adminEmail, subject, htmlContent);

            // Cập nhật trạng thái đã gửi thông báo
            message.setNotificationSent(true);
            contactMessageRepository.save(message);

            log.info("Đã gửi thông báo tin nhắn liên hệ mới cho admin (ID: {})", message.getId());
        } catch (Exception e) {
            // Ghi log lỗi nhưng không throw exception (đã lưu vào DB rồi)
            log.error("Không thể gửi thông báo email cho admin về tin nhắn ID {}: {}", message.getId(), e.getMessage());
            // Tin nhắn vẫn được lưu trong DB, admin có thể xem trong trang quản lý
        }
    }

    /**
     * Tạo nội dung email thông báo cho admin
     */
    private String buildAdminNotificationEmail(ContactMessage message) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .field { margin-bottom: 15px; }
                    .label { font-weight: bold; color: #555; }
                    .value { background: white; padding: 10px; border-radius: 4px; margin-top: 5px; border: 1px solid #eee; }
                    .message-content { background: white; padding: 15px; border-radius: 4px; border-left: 4px solid #4CAF50; }
                    .footer { text-align: center; padding: 15px; color: #777; font-size: 12px; }
                    .btn { display: inline-block; background: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>📬 Tin nhắn liên hệ mới</h2>
                    </div>
                    <div class="content">
                        <div class="field">
                            <div class="label">👤 Tên khách hàng:</div>
                            <div class="value">%s</div>
                        </div>
                        <div class="field">
                            <div class="label">📧 Email:</div>
                            <div class="value">%s</div>
                        </div>
                        %s
                        %s
                        <div class="field">
                            <div class="label">💬 Nội dung tin nhắn:</div>
                            <div class="message-content">%s</div>
                        </div>
                        <div class="field">
                            <div class="label">🕐 Thời gian gửi:</div>
                            <div class="value">%s</div>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Email này được gửi tự động từ hệ thống %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            escapeHtml(message.getName()),
            escapeHtml(message.getEmail()),
            message.getPhone() != null ? String.format("""
                <div class="field">
                    <div class="label">📱 Số điện thoại:</div>
                    <div class="value">%s</div>
                </div>
                """, escapeHtml(message.getPhone())) : "",
            message.getSubject() != null ? String.format("""
                <div class="field">
                    <div class="label">📋 Chủ đề:</div>
                    <div class="value">%s</div>
                </div>
                """, escapeHtml(message.getSubject())) : "",
            escapeHtml(message.getMessage()).replace("\n", "<br>"),
            message.getCreatedAt().toString(),
            storeName
        );
    }

    /**
     * Escape HTML để tránh XSS
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactMessageRepository.findAll(
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getContactsByStatus(ContactStatus status, Pageable pageable) {
        return contactMessageRepository.findByStatus(status,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getContactsByStatuses(List<ContactStatus> statuses, Pageable pageable) {
        return contactMessageRepository.findByStatusIn(statuses,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> searchContacts(String keyword, Pageable pageable) {
        return contactMessageRepository.searchByKeyword(keyword,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CONTACT_NOT_FOUND"));
        return mapToResponse(message);
    }

    @Override
    public ContactResponse updateContactStatus(Long id, ContactUpdateRequest request) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CONTACT_NOT_FOUND"));

        message.setStatus(request.getStatus());
        if (request.getAdminNote() != null) {
            message.setAdminNote(request.getAdminNote());
        }

        ContactMessage savedMessage = contactMessageRepository.save(message);
        log.info("Đã cập nhật trạng thái tin nhắn ID {} thành {}", id, request.getStatus());
        return mapToResponse(savedMessage);
    }

    @Override
    public ContactResponse replyToContact(Long id, ContactReplyRequest request, Long adminId) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CONTACT_NOT_FOUND"));

        // Cập nhật thông tin phản hồi
        message.setReplyContent(request.getReplyContent());
        message.setRepliedAt(LocalDateTime.now());
        message.setRepliedBy(adminId);
        message.setStatus(ContactStatus.REPLIED);

        ContactMessage savedMessage = contactMessageRepository.save(message);

        // Gửi email phản hồi cho khách hàng nếu được yêu cầu
        if (request.getSendEmail() != null && request.getSendEmail()) {
            sendReplyEmailAsync(savedMessage);
        }

        log.info("Admin {} đã phản hồi tin nhắn liên hệ ID {}", adminId, id);
        return mapToResponse(savedMessage);
    }

    /**
     * Gửi email phản hồi cho khách hàng (Async)
     */
    @Async("taskExecutor")
    public void sendReplyEmailAsync(ContactMessage message) {
        try {
            String subject = String.format("Phản hồi từ %s - %s", storeName,
                    message.getSubject() != null ? message.getSubject() : "Tin nhắn liên hệ");
            String htmlContent = buildReplyEmail(message);

            brevoEmailService.sendEmail(message.getEmail(), subject, htmlContent);
            log.info("Đã gửi email phản hồi cho khách hàng {} (Contact ID: {})", message.getEmail(), message.getId());
        } catch (Exception e) {
            log.error("Không thể gửi email phản hồi cho khách hàng {} (Contact ID: {}): {}",
                    message.getEmail(), message.getId(), e.getMessage());
        }
    }

    /**
     * Tạo nội dung email phản hồi cho khách hàng
     */
    private String buildReplyEmail(ContactMessage message) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .original-message { background: #eee; padding: 15px; border-radius: 4px; margin-bottom: 20px; border-left: 4px solid #999; }
                    .reply-content { background: white; padding: 15px; border-radius: 4px; border-left: 4px solid #4CAF50; }
                    .footer { text-align: center; padding: 15px; color: #777; font-size: 12px; }
                    .greeting { margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>📬 Phản hồi từ %s</h2>
                    </div>
                    <div class="content">
                        <div class="greeting">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Cảm ơn bạn đã liên hệ với chúng tôi. Dưới đây là phản hồi cho tin nhắn của bạn:</p>
                        </div>
                        
                        <p><strong>📝 Tin nhắn gốc của bạn:</strong></p>
                        <div class="original-message">%s</div>
                        
                        <p><strong>💬 Phản hồi của chúng tôi:</strong></p>
                        <div class="reply-content">%s</div>
                        
                        <p style="margin-top: 20px;">Nếu bạn có thêm câu hỏi, đừng ngần ngại liên hệ lại với chúng tôi.</p>
                        <p>Trân trọng,<br><strong>%s</strong></p>
                    </div>
                    <div class="footer">
                        <p>Email này được gửi từ %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            storeName,
            escapeHtml(message.getName()),
            escapeHtml(message.getMessage()).replace("\n", "<br>"),
            escapeHtml(message.getReplyContent()).replace("\n", "<br>"),
            storeName,
            storeName
        );
    }

    @Override
    public void deleteContact(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CONTACT_NOT_FOUND"));

        // Chỉ cho phép xóa tin nhắn đã archived
        if (message.getStatus() != ContactStatus.ARCHIVED) {
            throw new BadRequestException("Chỉ có thể xóa tin nhắn đã lưu trữ", "CONTACT_DELETE_NOT_ALLOWED");
        }

        contactMessageRepository.delete(message);
        log.info("Đã xóa tin nhắn liên hệ ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingMessages() {
        return contactMessageRepository.countPendingMessages();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getContactStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // Tổng số tin nhắn
        stats.put("total", contactMessageRepository.count());

        // Số tin nhắn theo trạng thái
        Map<String, Long> byStatus = new HashMap<>();
        for (ContactStatus status : ContactStatus.values()) {
            byStatus.put(status.name(), contactMessageRepository.countByStatus(status));
        }
        stats.put("byStatus", byStatus);

        // Thống kê theo ngày (nếu có khoảng thời gian)
        if (startDate != null && endDate != null) {
            List<Object[]> dailyStats = contactMessageRepository.countMessagesByDate(startDate, endDate);
            List<Map<String, Object>> dailyData = dailyStats.stream()
                    .map(row -> {
                        Map<String, Object> dayData = new HashMap<>();
                        dayData.put("date", row[0].toString());
                        dayData.put("count", row[1]);
                        return dayData;
                    })
                    .collect(Collectors.toList());
            stats.put("daily", dailyData);
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getRecentContacts(int limit) {
        return contactMessageRepository.findRecentMessages(PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Entity sang DTO Response
     */
    private ContactResponse mapToResponse(ContactMessage message) {
        return ContactResponse.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .phone(message.getPhone())
                .subject(message.getSubject())
                .message(message.getMessage())
                .status(message.getStatus())
                .adminNote(message.getAdminNote())
                .replyContent(message.getReplyContent())
                .repliedAt(message.getRepliedAt())
                .repliedBy(message.getRepliedBy())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}

