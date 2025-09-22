package com.foodorder.backend.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorder.backend.points.entity.RewardPoint;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnore // Tránh infinite recursion khi serialize JSON
    private Role role;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_verified")
    private boolean isVerified;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private RewardPoint rewardPoint;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Chạy trước khi INSERT → set giá trị mặc định
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        isActive = true; // Mặc định là hoạt động
        isVerified = false; // Chưa xác minh email
        // role sẽ được set từ bên ngoài khi tạo user
    }

    // Chạy trước khi UPDATE → cập nhật updatedAt
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra xem user có phải là admin không
     */
    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    /**
     * Kiểm tra xem user có phải là nhân viên không
     */
    public boolean isStaff() {
        return role != null && role.isStaff();
    }

    /**
     * Kiểm tra xem user có phải là khách hàng không
     */
    public boolean isCustomer() {
        return role != null && role.isCustomer();
    }

    /**
     * Lấy tên hiển thị của vai trò
     */
    public String getRoleDisplayName() {
        return role != null ? role.getName() : "Khách hàng";
    }

    /**
     * Lấy authority string cho Spring Security
     */
    public String getRoleAuthority() {
        return role != null ? role.getAuthority() : "ROLE_CUSTOMER";
    }

    /**
     * Lấy code của role
     */
    public String getRoleCode() {
        return role != null ? role.getCode() : "CUSTOMER";
    }
}