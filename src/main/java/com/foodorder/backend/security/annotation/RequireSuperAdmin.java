package com.foodorder.backend.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để kiểm tra quyền Super Admin
 * Chỉ cho phép user có role SUPER_ADMIN truy cập
 * Dùng cho các thao tác trên dữ liệu được bảo vệ (isProtected = true)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('SUPER_ADMIN')")
public @interface RequireSuperAdmin {
}

