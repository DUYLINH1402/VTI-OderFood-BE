package com.foodorder.backend.contact.dto;

import com.foodorder.backend.contact.entity.ContactStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO cho admin cập nhật trạng thái tin nhắn liên hệ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private ContactStatus status;

    /**
     * Ghi chú nội bộ của admin
     */
    private String adminNote;
}

