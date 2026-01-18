package com.foodorder.backend.points.controller;

import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.points.dto.response.PointsHistoryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.foodorder.backend.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * Controller quản lý điểm thưởng của người dùng
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "Points", description = "API quản lý điểm thưởng - Yêu cầu đăng nhập")
public class PointsController {
    private final PointsService pointsService;

    @Operation(summary = "Lấy điểm hiện tại", description = "Lấy số điểm thưởng hiện tại của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PointsResponseDTO> getCurrentPoints(
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        PointsResponseDTO points = pointsService.getCurrentPointsByUsername(username);
        return ResponseEntity.ok(points);
    }

    @Operation(summary = "Lịch sử điểm thưởng", description = "Lấy lịch sử sử dụng điểm thưởng của người dùng với phân trang.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PointsHistoryDTO>> getPointsHistory(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 10) Pageable pageable) {
        String username = authentication.getName();
        Page<PointsHistoryDTO> history = pointsService.getPointsHistoryByUsername(username, pageable);
        return ResponseEntity.ok(history);
    }
}
