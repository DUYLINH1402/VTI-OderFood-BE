package com.foodorder.backend.feedbacks.controller;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý nội dung phản hồi/đánh giá
 */
@RestController
@RequestMapping("/api/feedback-media")
@Tag(name = "Feedback Media", description = "API quản lý nội dung phản hồi - Hình ảnh, video đánh giá")
public class FeedbackMediaController {

    @Autowired
    private FeedbackMediaService service;

    @Operation(summary = "Lấy tất cả feedback media", description = "Lấy danh sách tất cả nội dung phản hồi.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public List<FeedbackMediaResponse> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Chi tiết feedback media", description = "Lấy thông tin chi tiết một nội dung phản hồi theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public FeedbackMediaResponse getById(
            @Parameter(description = "ID của feedback media") @PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Tạo feedback media (Admin)", description = "Tạo mới nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping
    public FeedbackMediaResponse create(@RequestBody FeedbackMediaRequest req) {
        return service.create(req);
    }

    @Operation(summary = "Cập nhật feedback media (Admin)", description = "Cập nhật nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PutMapping("/{id}")
    public FeedbackMediaResponse update(
            @Parameter(description = "ID của feedback media") @PathVariable Long id,
            @RequestBody FeedbackMediaRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Xóa feedback media (Admin)", description = "Xóa nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID của feedback media") @PathVariable Long id) {
        service.delete(id);
    }
}

