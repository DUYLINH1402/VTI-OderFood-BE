package com.foodorder.backend.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "forgot_password_requests")
public class ForgotPasswordRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt = LocalDateTime.now();

}
