package com.foodorder.backend.order.entity;

import com.foodorder.backend.food.entity.Food;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false, updatable = false, insertable = false)
    private Long orderId;

    @NotNull
    @Column(name = "food_id", nullable = false, updatable = false, insertable = false)
    private Long foodId;

    @Column(name = "food_name", length = 255)
    private String foodName;

    @Column(name = "food_slug", length = 255)
    private String foodSlug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;


    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
}
