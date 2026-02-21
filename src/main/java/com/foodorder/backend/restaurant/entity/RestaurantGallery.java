package com.foodorder.backend.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ hình ảnh gallery của nhà hàng
 * Quan hệ Many-to-One với RestaurantInfo
 */
@Entity
@Table(name = "restaurant_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Quan hệ Many-to-One với RestaurantInfo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_info_id")
    private RestaurantInfo restaurantInfo;
}

