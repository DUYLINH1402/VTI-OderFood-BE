package com.foodorder.backend.feedbacks.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
@Entity
@Table(name = "feedback_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type; // IMAGE hoặc VIDEO

    @Column(name = "media_url", length = 255, nullable = false)
    private String mediaUrl;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Enum type
    public enum MediaType {
        IMAGE, VIDEO
    }
}