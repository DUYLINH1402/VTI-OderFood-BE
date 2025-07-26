package com.foodorder.backend.cart.entity;

import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "id", nullable = true)
    private FoodVariant variant;


    // Constructor custom cho addToCart
    public CartItem(User user, Food food, int quantity) {
        this.user = user;
        this.food = food;
        this.quantity = quantity;
    }

}
