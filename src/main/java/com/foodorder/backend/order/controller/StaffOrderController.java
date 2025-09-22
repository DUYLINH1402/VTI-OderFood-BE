package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.StaffOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * Controller dành riêng cho STAFF quản lý đơn hàng
 * Tập trung vào các thao tác xử lý đơn hàng hàng ngày
 */
@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_ADMIN')")
public class StaffOrderController {

    private final StaffOrderService staffOrderService;

    /**
     * Lấy danh sách đơn hàng cần xác nhận (PROCESSING - đã thanh toán, chờ xác nhận)
     */
    @GetMapping("/need-confirmation")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersNeedConfirmation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        PageResponse<OrderResponse> orders = staffOrderService.getOrdersNeedConfirmation(pageRequest);

        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy đơn hàng đang xử lý
     */
    @GetMapping("/processing")
    public ResponseEntity<PageResponse<OrderResponse>> getProcessingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        PageResponse<OrderResponse> orders = staffOrderService.getProcessingOrders(pageRequest);

        return ResponseEntity.ok(orders);
    }

    /**
     * Cập nhật trạng thái đơn hàng (STAFF) - sử dụng orderCode
     */
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderCode,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse updatedOrder = staffOrderService.updateOrderStatusByCode(orderCode, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(updatedOrder)
                .build());
    }

    /**
     * Lấy chi tiết đơn hàng (STAFF)
     * Có thể tìm kiếm theo ID đơn hàng hoặc mã đơn hàng
     */
    @GetMapping("/{orderIdOrCode}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable String orderIdOrCode) {

        OrderResponse order = staffOrderService.getOrderDetails(orderIdOrCode);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .data(order)
                .build());
    }

    /**
     * Lấy danh sách đơn hàng gần đây với nhiều tùy chọn lọc và phân trang
     * API chính cho staff xem đơn hàng trong khoảng thời gian gần đây
     */
    @GetMapping("/recent")
    public ResponseEntity<PageResponse<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "365") int days,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // Tạo Sort object dựa trên sortBy và sortDirection
        Sort sort = sortDirection.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = staffOrderService.getRecentOrdersWithFilter(
                pageRequest, days, status, search);

        return ResponseEntity.ok(orders);
    }
}
