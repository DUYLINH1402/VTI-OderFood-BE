package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request cập nhật trạng thái đơn hàng bởi Staff/Admin
 */
@Data
public class ManagementUpdateStatusRequest {
    
    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;
    
    private String note; // Ghi chú của staff
    
    private String internalNote; // Ghi chú nội bộ (chỉ staff/admin thấy)
    
    private boolean notifyCustomer = true; // Có thông báo cho khách hàng không
}
