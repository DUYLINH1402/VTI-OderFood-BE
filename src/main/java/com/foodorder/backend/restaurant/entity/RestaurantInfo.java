package com.foodorder.backend.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity lưu trữ thông tin cơ sở nhà hàng
 * Hiện tại chỉ có một cơ sở duy nhất (ID = 1)
 */
@Entity
@Table(name = "restaurant_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "opening_hours")
    private String openingHours;

    // Quan hệ One-to-Many với RestaurantGallery
    @OneToMany(mappedBy = "restaurantInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<RestaurantGallery> galleries = new ArrayList<>();

    // Helper method để thêm gallery
    public void addGallery(RestaurantGallery gallery) {
        galleries.add(gallery);
        gallery.setRestaurantInfo(this);
    }

    // Helper method để xóa gallery
    public void removeGallery(RestaurantGallery gallery) {
        galleries.remove(gallery);
        gallery.setRestaurantInfo(null);
    }
}
