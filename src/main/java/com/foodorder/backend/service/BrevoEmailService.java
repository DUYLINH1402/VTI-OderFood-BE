
package com.foodorder.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrevoEmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String htmlContent) {
        System.out.println("BREVO CONFIG:");
        System.out.println("API KEY = " + apiKey);
        System.out.println("SENDER  = " + senderEmail);
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("email", senderEmail, "name", "Dong Xanh Food"));
        payload.put("to", new Map[]{ Map.of("email", to) });
        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(url, request, String.class);
    }
}
