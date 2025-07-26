package com.foodorder.backend.category.entity;
import com.foodorder.backend.food.entity.Food;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String slug;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Liên kết với danh mục cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    // Liên kết với danh mục con
    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    // Một category có thể có nhiều món ăn
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Food> foods;

}

