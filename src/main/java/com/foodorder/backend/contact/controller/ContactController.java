package com.foodorder.backend.contact.controller;

import com.foodorder.backend.contact.dto.ContactRequest;
import com.foodorder.backend.contact.dto.ContactResponse;
import com.foodorder.backend.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý API gửi tin nhắn liên hệ từ khách hàng (Public)
 * Không yêu cầu đăng nhập để khách hàng có thể liên hệ dễ dàng
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor

@Slf4j
@Tag(name = "Contact", description = "API gửi tin nhắn liên hệ")
public class ContactController {

    private final ContactService contactService;

    /**
     * Gửi tin nhắn liên hệ từ khách hàng
     * - Kiểm tra rate limiting để chống spam
     * - Lưu tin nhắn vào DB ngay lập tức
     * - Gửi thông báo cho admin (async)
     */
    @Operation(summary = "Gửi tin nhắn liên hệ",
               description = "Khách hàng gửi tin nhắn liên hệ. Rate limit: 3 tin/phút/IP, 5 tin/giờ/email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Tin nhắn đã được tiếp nhận và đang xử lý"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "429", description = "Gửi quá nhiều tin nhắn (rate limit)")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContact(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        // Lấy IP address của người gửi (hỗ trợ proxy)
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Nhận tin nhắn liên hệ từ IP: {}, Email: {}", ipAddress, request.getEmail());

        // Xử lý tin nhắn (có rate limiting)
        ContactResponse response = contactService.submitContact(request, ipAddress);

        // Trả về 202 Accepted - tin nhắn đã nhận, đang xử lý thông báo ngầm
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "success", true,
                        "message", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể.",
                        "contactId", response.getId()
                ));
    }

    /**
     * Lấy IP address thực của client (hỗ trợ qua proxy/load balancer)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}

