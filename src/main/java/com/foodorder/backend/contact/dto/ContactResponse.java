package com.foodorder.backend.contact.dto;

import com.foodorder.backend.contact.entity.ContactStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin tin nhắn liên hệ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private String adminNote;
    private String replyContent;
    private LocalDateTime repliedAt;
    private Long repliedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

