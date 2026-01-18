package com.foodorder.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép các origin
        config.setAllowedOriginPatterns(Arrays.asList(
                "https://oder-4c1f2.web.app",
                "https://dongxanhfood.shop",
                "http://localhost:5173",
                "https://dongxanhfoodorder.shop"
        ));

        // Cho phép các method
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cho phép các header
        config.setAllowedHeaders(Arrays.asList("*"));

        // Cho phép credentials
        config.setAllowCredentials(true);

        // Áp dụng cho tất cả các endpoint
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}