package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.AdminCancelOrderRequest;
import com.foodorder.backend.order.dto.request.UpdateInternalNoteRequest;
import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.AdminDashboardStatsResponse;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Orders", description = "API quản lý đơn hàng dành cho Admin - Quyền cao nhất")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "Tất cả đơn hàng", description = "Lấy tất cả đơn hàng với nhiều bộ lọc (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Trạng thái đơn hàng") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Mã đơn hàng") @RequestParam(required = false) String orderCode,
            @Parameter(description = "Tên khách hàng") @RequestParam(required = false) String customerName,
            @Parameter(description = "Ngày bắt đầu") @RequestParam(required = false) String startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam(required = false) String endDate) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = adminOrderService.getAllOrdersWithFilters(
                status, orderCode, customerName, startDate, endDate, null, pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Thống kê đơn hàng", description = "Lấy thống kê tổng quan về đơn hàng (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics(
            @Parameter(description = "Ngày bắt đầu") @RequestParam(required = false) String startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam(required = false) String endDate,
            @Parameter(description = "Khoảng thời gian") @RequestParam(required = false) String period) {

        OrderStatisticsResponse statistics = adminOrderService.getOrderStatistics(startDate, endDate, period);

        return ResponseEntity.ok(ApiResponse.<OrderStatisticsResponse>builder()
                .success(true)
                .message("Lấy thống kê đơn hàng thành công")
                .data(statistics)
                .build());
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái đơn hàng với quyền cao nhất (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse updatedOrder = adminOrderService.updateOrderStatusWithFullAccess(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Xóa đơn hàng", description = "Xóa đơn hàng (soft delete) - Chỉ Admin.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId) {
        adminOrderService.deleteOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa đơn hàng thành công")
                .build());
    }

    @Operation(summary = "Khôi phục đơn hàng", description = "Khôi phục đơn hàng đã bị hủy (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Khôi phục thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PostMapping("/{orderId}/restore")
    public ResponseEntity<ApiResponse<OrderResponse>> restoreOrder(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId) {
        OrderResponse order = adminOrderService.restoreOrder(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Khôi phục đơn hàng thành công")
                .data(order)
                .build());
    }

    @Operation(summary = "Chi tiết đơn hàng đầy đủ", description = "Lấy chi tiết đơn hàng với thông tin đầy đủ (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @GetMapping("/{orderId}/details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderFullDetails(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId) {

        OrderResponse order = adminOrderService.getOrderFullDetails(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Lấy chi tiết đơn hàng thành công")
                .data(order)
                .build());
    }

    @Operation(summary = "Tìm kiếm nâng cao", description = "Tìm kiếm đơn hàng nâng cao với nhiều tiêu chí (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/advanced-search")
    public ResponseEntity<PageResponse<OrderResponse>> advancedSearch(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái") @RequestParam(required = false) String status,
            @Parameter(description = "Email khách hàng") @RequestParam(required = false) String customerEmail,
            @Parameter(description = "SĐT khách hàng") @RequestParam(required = false) String customerPhone,
            @Parameter(description = "Giá trị tối thiểu") @RequestParam(required = false) Double minAmount,
            @Parameter(description = "Giá trị tối đa") @RequestParam(required = false) Double maxAmount,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<OrderResponse> orders = adminOrderService.advancedSearch(
                keyword, status, customerEmail, customerPhone, minAmount, maxAmount, pageRequest);

        return ResponseEntity.ok(orders);
    }

    // ============ CÁC API MỚI CHO ADMIN ============

    @Operation(summary = "Cập nhật ghi chú nội bộ", description = "Cập nhật ghi chú nội bộ cho đơn hàng (chỉ Admin/Staff thấy).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PutMapping("/{orderId}/internal-note")
    public ResponseEntity<ApiResponse<OrderResponse>> updateInternalNote(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId,
            @Valid @RequestBody UpdateInternalNoteRequest request) {

        log.info("Admin cập nhật ghi chú nội bộ cho đơn hàng ID: {}", orderId);
        OrderResponse updatedOrder = adminOrderService.updateInternalNote(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật ghi chú nội bộ thành công")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Hủy đơn hàng với lý do", description = "Hủy đơn hàng kèm lý do chi tiết (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderWithReason(
            @Parameter(description = "ID đơn hàng") @PathVariable Long orderId,
            @Valid @RequestBody AdminCancelOrderRequest request) {

        log.info("Admin hủy đơn hàng ID: {} với lý do: {}", orderId, request.getCancelReason());
        OrderResponse cancelledOrder = adminOrderService.cancelOrderWithReason(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Hủy đơn hàng thành công")
                .data(cancelledOrder)
                .build());
    }

    @Operation(summary = "Thống kê Dashboard", description = "Lấy thống kê chuyên sâu cho Dashboard Admin (doanh thu, đơn hủy, v.v.).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {

        log.info("Admin lấy thống kê dashboard");
        AdminDashboardStatsResponse stats = adminOrderService.getDashboardStats();

        return ResponseEntity.ok(ApiResponse.<AdminDashboardStatsResponse>builder()
                .success(true)
                .message("Lấy thống kê dashboard thành công")
                .data(stats)
                .build());
    }
}
