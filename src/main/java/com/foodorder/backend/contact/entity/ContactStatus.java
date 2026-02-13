package com.foodorder.backend.contact.entity;

/**
 * Enum định nghĩa trạng thái của tin nhắn liên hệ
 */
public enum ContactStatus {
    /**
     * Tin nhắn mới, chưa đọc
     */
    PENDING,

    /**
     * Đã đọc nhưng chưa phản hồi
     */
    READ,

    /**
     * Đã phản hồi cho khách hàng
     */
    REPLIED,

    /**
     * Đã lưu trữ (xong việc)
     */
    ARCHIVED
}

