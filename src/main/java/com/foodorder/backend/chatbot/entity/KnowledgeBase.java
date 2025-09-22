package com.foodorder.backend.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ knowledge base cho hệ thống RAG
 * Chứa các thông tin về nhà hàng, thực đơn, quy định, FAQ...
 */
@Entity
@Table(name = "knowledge_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title; // Tiêu đề của thông tin

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // Nội dung chi tiết

    @Column(name = "keywords", length = 1000)
    private String keywords; // Từ khóa để tìm kiếm, cách nhau bởi dấu phẩy

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private KnowledgeCategory category;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 1; // Độ ưu tiên (1-10), số càng cao càng ưu tiên

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy; // Admin tạo ra

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum KnowledgeCategory {
        RESTAURANT_INFO("Thông tin nhà hàng"),
        MENU_INFO("Thông tin thực đơn"),
        ORDER_POLICY("Chính sách đặt hàng"),
        PAYMENT_INFO("Thông tin thanh toán"),
        DELIVERY_INFO("Thông tin giao hàng"),
        PROMOTION("Khuyến mãi"),
        FAQ("Câu hỏi thường gặp"),
        CONTACT("Thông tin liên hệ"),
        OPERATING_HOURS("Giờ hoạt động"),
        OTHER("Khác");

        private final String displayName;

        KnowledgeCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
