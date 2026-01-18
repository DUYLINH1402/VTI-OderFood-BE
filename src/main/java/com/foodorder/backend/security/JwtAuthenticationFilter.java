package com.foodorder.backend.security;

import com.foodorder.backend.security.exception.JwtTokenExpiredException;
import com.foodorder.backend.security.exception.JwtTokenInvalidException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Danh sách các URL patterns không cần qua JWT filter (kiểm tra startsWith)
     */
    private static final List<String> EXCLUDED_URL_PREFIXES = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/configuration"
    );

    /**
     * Bỏ qua filter cho các URL của Swagger và các public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Kiểm tra nếu path bắt đầu bằng bất kỳ prefix nào trong danh sách
        boolean shouldSkip = EXCLUDED_URL_PREFIXES.stream().anyMatch(path::startsWith);

        if (shouldSkip) {
            log.debug("Skipping JWT filter for path: {}", path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Bắt exception khi token hết hạn hoặc không hợp lệ
                String username = jwtUtil.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(token)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JwtTokenExpiredException e) {
                // Log lỗi token hết hạn nhưng không crash server
                log.warn("JWT token expired for request: {} {}", request.getMethod(), request.getRequestURI());
                // Không set authentication, để endpoint trả về 401 nếu cần auth
            } catch (JwtTokenInvalidException e) {
                // Log lỗi token không hợp lệ
                log.warn("Invalid JWT token for request: {} {}", request.getMethod(), request.getRequestURI());
            } catch (Exception e) {
                // Bắt tất cả các exception khác liên quan đến JWT
                log.error("Unexpected error while processing JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
