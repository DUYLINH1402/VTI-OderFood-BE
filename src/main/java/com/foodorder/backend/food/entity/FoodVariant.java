package com.foodorder.backend.food.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "food_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "extra_price")
    private BigDecimal extraPrice;

    @Column(name = "is_default")
    private Boolean isDefault;
}

