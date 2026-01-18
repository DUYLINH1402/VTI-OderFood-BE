package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.StaffOrderService;
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
 * Controller dành riêng cho STAFF quản lý đơn hàng
 * Tập trung vào các thao tác xử lý đơn hàng hàng ngày
 */
@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_STAFF') or hasAuthority('ROLE_ADMIN')")
@Tag(name = "Staff Orders", description = "API quản lý đơn hàng dành cho Staff")
public class StaffOrderController {

    private final StaffOrderService staffOrderService;

    @Operation(summary = "Đơn hàng cần xác nhận", description = "Lấy danh sách đơn hàng đã thanh toán, đang chờ xác nhận (PROCESSING).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền Staff")
    })
    @GetMapping("/need-confirmation")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersNeedConfirmation(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        PageResponse<OrderResponse> orders = staffOrderService.getOrdersNeedConfirmation(pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Đơn hàng đang xử lý", description = "Lấy danh sách đơn hàng đang trong quá trình xử lý.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/processing")
    public ResponseEntity<PageResponse<OrderResponse>> getProcessingOrders(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        PageResponse<OrderResponse> orders = staffOrderService.getProcessingOrders(pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái đơn hàng theo mã đơn (Staff).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse updatedOrder = staffOrderService.updateOrderStatusByCode(orderCode, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Chi tiết đơn hàng", description = "Lấy chi tiết đơn hàng theo ID hoặc mã đơn.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @GetMapping("/{orderIdOrCode}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @Parameter(description = "ID hoặc mã đơn hàng") @PathVariable String orderIdOrCode) {

        OrderResponse order = staffOrderService.getOrderDetails(orderIdOrCode);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .data(order)
                .build());
    }

    @Operation(summary = "Đơn hàng gần đây", description = "Lấy danh sách đơn hàng gần đây với nhiều tùy chọn lọc và phân trang.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/recent")
    public ResponseEntity<PageResponse<OrderResponse>> getRecentOrders(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "Số ngày gần đây") @RequestParam(defaultValue = "365") int days,
            @Parameter(description = "Trạng thái đơn hàng") @RequestParam(required = false) String status,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String search,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = staffOrderService.getRecentOrdersWithFilter(
                pageRequest, days, status, search);

        return ResponseEntity.ok(orders);
    }
}
