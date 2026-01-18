package com.foodorder.backend.order.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc cập nhật ghi chú nội bộ (internal_note)
 * Dùng cho đối soát, lưu ý về dòng tiền hoặc khách hàng mà chỉ nội bộ quản trị thấy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInternalNoteRequest {

    @Size(max = 2000, message = "Ghi chú nội bộ không được vượt quá 2000 ký tự")
    private String internalNote;
}

