package com.foodorder.backend.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho admin phản hồi tin nhắn liên hệ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(min = 10, max = 5000, message = "Nội dung phản hồi phải từ 10 đến 5000 ký tự")
    private String replyContent;

    /**
     * Có gửi email phản hồi cho khách hàng không
     */
    private Boolean sendEmail = true;
}

