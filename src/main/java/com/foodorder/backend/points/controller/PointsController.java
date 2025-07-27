package com.foodorder.backend.points.controller;

import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.points.dto.response.PointsHistoryDTO;
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

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {
    private final PointsService pointsService;

    // API lấy điểm hiện tại của user đang đăng nhập
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PointsResponseDTO> getCurrentPoints(Authentication authentication) {
        String username = authentication.getName();
        PointsResponseDTO points = pointsService.getCurrentPointsByUsername(username);
        return ResponseEntity.ok(points);
    }

    // API lấy lịch sử sử dụng điểm của user đang đăng nhập (có phân trang)
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PointsHistoryDTO>> getPointsHistory(Authentication authentication,
                                                                 @PageableDefault(size = 10) Pageable pageable) {
        String username = authentication.getName();
        Page<PointsHistoryDTO> history = pointsService.getPointsHistoryByUsername(username, pageable);
        return ResponseEntity.ok(history);
    }
}
