package com.foodorder.backend.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entity cho bảng roles
 * Quản lý thông tin vai trò người dùng với id, code, name
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code; // , STAFF, ADMIN

    @Column(name = "name", nullable = false, length = 50)
    private String name; // Khách hàng, Nhân viên, Quản trị viên

    // Relationship với User - Thêm @JsonIgnore để tránh infinite recursion
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore // Không serialize trường này khi trả về JSON
    private List<User> users;

    /**
     * Lấy authority cho Spring Security
     * Vì code đã chứa tiền tố ROLE_ (ví dụ: ROLE_ADMIN), nên trả về trực tiếp
     */
    public String getAuthority() {
        return code; // Trả về trực tiếp code vì đã có tiền tố ROLE_
    }

    /**
     * Kiểm tra xem có phải role admin không
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(code);
    }

    /**
     * Kiểm tra xem có phải role staff không
     */
    public boolean isStaff() {
        return "ROLE_STAFF".equals(code);
    }

    /**
     * Kiểm tra xem có phải role customer không
     */
    public boolean isCustomer() {
        return "ROLE_USER".equals(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
