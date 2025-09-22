package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * Controller dành riêng cho ADMIN quản lý đơn hàng
 * Tập trung vào quản lý tổng thể, thống kê và giám sát
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * Lấy tất cả đơn hàng với bộ lọc đa dạng (ADMIN)
     */
    @GetMapping("/all")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Admin getting all orders with filters - status: {}, page: {}", status, page);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = adminOrderService.getAllOrdersWithFilters(
                status, orderCode, customerName, startDate, endDate, null, pageRequest);

        return ResponseEntity.ok(orders);
    }

    /**
     * Thống kê đơn hàng tổng quan (ADMIN)
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period) {

        log.info("Admin getting order statistics - period: {}", period);

        OrderStatisticsResponse statistics = adminOrderService.getOrderStatistics(startDate, endDate, period);

        return ResponseEntity.ok(ApiResponse.<OrderStatisticsResponse>builder()
                .success(true)
                .message("Lấy thống kê đơn hàng thành công")
                .data(statistics)
                .build());
    }

    /**
     * Cập nhật trạng thái đơn hàng với quyền cao nhất (ADMIN)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info("Admin updating order {} to status {}", orderId, request.getStatus());

        OrderResponse updatedOrder = adminOrderService.updateOrderStatusWithFullAccess(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(updatedOrder)
                .build());
    }

    /**
     * Xóa đơn hàng (ADMIN only - soft delete)
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long orderId) {

        log.warn("Admin deleting order {}", orderId);

        adminOrderService.deleteOrder(orderId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa đơn hàng thành công")
                .build());
    }

    /**
     * Khôi phục đơn hàng đã hủy (ADMIN)
     */
    @PostMapping("/{orderId}/restore")
    public ResponseEntity<ApiResponse<OrderResponse>> restoreOrder(@PathVariable Long orderId) {

        log.info("Admin restoring order {}", orderId);

        OrderResponse order = adminOrderService.restoreOrder(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Khôi phục đơn hàng thành công")
                .data(order)
                .build());
    }

    /**
     * Lấy chi tiết đơn hàng với thông tin đầy đủ (ADMIN)
     */
    @GetMapping("/{orderId}/details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderFullDetails(@PathVariable Long orderId) {

        log.info("Admin getting full order details: {}", orderId);

        OrderResponse order = adminOrderService.getOrderFullDetails(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Lấy chi tiết đơn hàng thành công")
                .data(order)
                .build());
    }

    /**
     * Tìm kiếm đơn hàng nâng cao (ADMIN)
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<PageResponse<OrderResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin performing advanced search with keyword: {}", keyword);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<OrderResponse> orders = adminOrderService.advancedSearch(
                keyword, status, customerEmail, customerPhone, minAmount, maxAmount, pageRequest);

        return ResponseEntity.ok(orders);
    }
}
