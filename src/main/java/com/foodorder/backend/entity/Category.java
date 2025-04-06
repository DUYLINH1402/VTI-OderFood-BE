package com.foodorder.backend.entity;
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
    private String name;
    private String description;

    // Một category có thể có nhiều món ăn
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Food> foods;
}

