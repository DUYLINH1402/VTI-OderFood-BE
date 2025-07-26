package com.foodorder.backend.auth.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "change_password_attempts")
public class ChangePasswordAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt = LocalDateTime.now();

}
