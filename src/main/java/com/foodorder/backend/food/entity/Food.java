package com.foodorder.backend.food.entity;
import com.foodorder.backend.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        @Column(name = "slug", unique = true,length = 255, nullable = false)
        private String slug;

        @Column(name = "description", length = 1000)
        private String description;

        @Column(name = "parent_id")
        private Long parentId;

        @Column(name = "price")
        private BigDecimal price;

        @Column(name = "image_url")
        private String imageUrl;

        @Column(name = "is_best_seller")
        private Boolean isBestSeller;

        @Column(name = "is_new")
        private Boolean isNew;

        @Column(name = "is_featured")
        private Boolean isFeatured;

        @Column(name = "total_sold")
        private Integer totalSold;

        @Column(name = "stock_quantity")
        private Integer stockQuantity;

        @Column(name = "is_active")
        private Boolean isActive;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category;

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "foodId")
        private List<FoodImage> images;

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "foodId")
        private List<FoodVariant> variants;

}
