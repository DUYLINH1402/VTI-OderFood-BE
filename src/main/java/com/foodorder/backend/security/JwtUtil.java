package com.foodorder.backend.security;

import com.foodorder.backend.security.exception.JwtTokenExpiredException;
import com.foodorder.backend.security.exception.JwtTokenInvalidException;
import com.foodorder.backend.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

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
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException("JWT_TOKEN_EXPIRED");
        } catch (UnsupportedJwtException | MalformedJwtException | io.jsonwebtoken.security.SignatureException
                | IllegalArgumentException e) {
            throw new JwtTokenInvalidException("JWT_TOKEN_INVALID");
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
