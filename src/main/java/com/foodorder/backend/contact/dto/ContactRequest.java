package com.foodorder.backend.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho request gửi tin nhắn liên hệ từ khách hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {

    @NotBlank(message = "Vui lòng nhập tên của bạn")
    @Size(min = 2, max = 100, message = "Tên phải từ 2 đến 100 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phone;

    @Size(max = 200, message = "Chủ đề không được quá 200 ký tự")
    private String subject;

    @NotBlank(message = "Vui lòng nhập nội dung tin nhắn")
    @Size(min = 10, max = 5000, message = "Nội dung phải từ 10 đến 5000 ký tự")
    private String message;
}

