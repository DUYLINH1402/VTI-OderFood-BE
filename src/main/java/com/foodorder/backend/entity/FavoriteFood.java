package com.foodorder.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "favorite_foods", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "food_id", "variant_id"})
})
public class FavoriteFood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private FoodVariant variant;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
