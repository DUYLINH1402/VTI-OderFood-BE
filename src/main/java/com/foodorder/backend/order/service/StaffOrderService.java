package com.foodorder.backend.order.service;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import org.springframework.data.domain.PageRequest;

/**
 * Service dành riêng cho STAFF xử lý đơn hàng
 * Tập trung vào workflow xử lý đơn hàng hàng ngày
 */
public interface StaffOrderService {

    /**
     * Lấy danh sách đơn hàng cần xác nhận (PROCESSING - đã thanh toán, chờ xác nhận)
     */
    PageResponse<OrderResponse> getOrdersNeedConfirmation(PageRequest pageRequest);

    /**
     * Lấy đơn hàng đang xử lý (CONFIRMED, DELIVERING - đang chế biến/giao hàng)
     */
    PageResponse<OrderResponse> getProcessingOrders(PageRequest pageRequest);

    /**
     * Lấy danh sách đơn hàng trong 7 ngày gần đây (loại trừ PENDING - chỉ Admin mới thấy)
     */
    PageResponse<OrderResponse> getRecentOrders(PageRequest pageRequest);

    /**
     * Lấy danh sách đơn hàng gần đây với bộ lọc nâng cao
     * @param pageRequest thông tin phân trang và sắp xếp
     * @param days số ngày gần đây (mặc định 7 ngày)
     * @param status lọc theo trạng thái đơn hàng (tùy chọn)
     * @param search tìm kiếm theo mã đơn, tên khách hàng hoặc email (tùy chọn)
     */
    PageResponse<OrderResponse> getRecentOrdersWithFilter(PageRequest pageRequest, int days, String status, String search);

    /**
     * Cập nhật trạng thái đơn hàng bằng orderCode (chỉ các trạng thái được phép)
     */
    OrderResponse updateOrderStatusByCode(String orderCode, UpdateOrderStatusRequest request);

    /**
     * Lấy chi tiết đơn hàng theo ID hoặc mã đơn hàng
     * Tự động phát hiện và xử lý cả hai trường hợp
     */
    OrderResponse getOrderDetails(String orderIdOrCode);
}
