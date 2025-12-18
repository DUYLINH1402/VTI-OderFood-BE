package com.foodorder.backend.security;

import com.foodorder.backend.security.exception.JwtTokenExpiredException;
import com.foodorder.backend.security.exception.JwtTokenInvalidException;
import com.foodorder.backend.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    // Khoá bí mật JWT – nên để trong biến môi trường
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_MS;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRoleAuthority()) // Sử dụng String authority thay vì entity Role
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            System.out.println("Không thể lấy username từ token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy JWT token từ HTTP request header Authorization
     * @param request HTTP request
     * @return JWT token string hoặc null nếu không tìm thấy
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Loại bỏ "Bearer " prefix
        }

        return null;
    }

    /**
     * Lấy JWT token từ WebSocket headers (tái sử dụng logic từ getTokenFromRequest)
     * @param headerAccessor WebSocket message header accessor
     * @return JWT token string hoặc null nếu không tìm thấy
     */
    public String getTokenFromWebSocketHeaders(org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        // Thử lấy từ session attributes trước (đã được set khi connect)
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            String token = (String) sessionAttrs.get("token");
            if (token != null && !token.trim().isEmpty()) {
                return token.trim();
            }
        }

        // Nếu không có trong session, lấy từ native headers (tái sử dụng logic có sẵn)
        java.util.List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authorizationHeader = authHeaders.get(0);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7); // Tái sử dụng logic từ getTokenFromRequest
            }
        }

        return null;
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                System.out.println("Token validation failed: Token is null or empty");
                return false;
            }
            parseClaims(token);
            return true;
        } catch (JwtTokenExpiredException e) {
            System.out.println("Token validation failed: Token expired - " + e.getMessage());
            return false;
        } catch (JwtTokenInvalidException e) {
            System.out.println("Token validation failed: Invalid token - " + e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            System.out.println("Token validation failed: Token expired - " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException |
                 io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
            System.out.println("Token validation failed: Invalid token - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Token validation failed: Unexpected error - " + e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException("JWT_TOKEN_EXPIRED");
        } catch (UnsupportedJwtException | MalformedJwtException | io.jsonwebtoken.security.SignatureException
                | IllegalArgumentException e) {
            throw new JwtTokenInvalidException("JWT_TOKEN_INVALID");
        }
    }
}
