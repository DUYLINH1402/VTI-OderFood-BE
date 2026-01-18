
package com.foodorder.backend.service;
import com.foodorder.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BrevoEmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String htmlContent) {
        // Log API key một cách an toàn
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("API key is NULL or EMPTY!");
            throw new BadRequestException("API key chưa được cấu hình", "EMAIL_CONFIG_ERROR");
        }
        String url = "https://api.brevo.com/v3/smtp/email";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("api-key", apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("email", senderEmail, "name", "Dong Xanh Food"));
        payload.put("to", new Map[]{ Map.of("email", to) });
        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("=== LỖI 401 UNAUTHORIZED ===");
            log.error("Chi tiết: API key không hợp lệ hoặc đã hết hạn");
            log.error("Response body: {}", e.getResponseBodyAsString());
            throw new BadRequestException("Không thể gửi email - API key không hợp lệ", "EMAIL_SERVICE_AUTH_ERROR");
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("=== LỖI 400 BAD REQUEST ===");
            log.error("Response body: {}", e.getResponseBodyAsString());
            throw new BadRequestException("Không thể gửi email - Yêu cầu không hợp lệ: " + e.getResponseBodyAsString(), "EMAIL_BAD_REQUEST");
        } catch (HttpClientErrorException e) {
            log.error("=== LỖI HTTP CLIENT {} ===", e.getStatusCode());
            log.error("Response body: {}", e.getResponseBodyAsString());
            throw new BadRequestException("Không thể gửi email: " + e.getResponseBodyAsString(), "EMAIL_SEND_FAILED");
        } catch (HttpServerErrorException e) {
            log.error("=== LỖI HTTP SERVER {} ===", e.getStatusCode());
            log.error("Response body: {}", e.getResponseBodyAsString());
            throw new BadRequestException("Lỗi server Brevo: " + e.getStatusCode(), "EMAIL_SERVER_ERROR");
        } catch (Exception e) {
            log.error("=== LỖI KHÔNG XÁC ĐỊNH ===");
            log.error("Loại lỗi: {}", e.getClass().getName());
            log.error("Chi tiết: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể gửi email: " + e.getMessage(), "EMAIL_SEND_FAILED");
        }
    }
}
