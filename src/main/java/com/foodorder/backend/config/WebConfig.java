//package com.foodorder.backend.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**")
//                // Sử dụng allowedOriginPatterns thay vì allowedOrigins khi có allowCredentials(true)
//                .allowedOriginPatterns(
//                        "https://oder-4c1f2.web.app",
//                        "http://localhost:5173",
//                        "https://dongxanhfoodorder.shop",
//                        "https://dongxanhfood.shop"
//                )
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .maxAge(3600); // Cache preflight response for 1 hour
//    }
//}