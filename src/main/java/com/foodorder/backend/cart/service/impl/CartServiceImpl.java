package com.foodorder.backend.cart.service.impl;

import com.foodorder.backend.cart.dto.request.CartRequest;
import com.foodorder.backend.cart.dto.response.CartResponse;
import com.foodorder.backend.cart.entity.CartItem;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.cart.repository.CartItemRepository;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.food.repository.FoodVariantRepository;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.cart.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final FoodVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Override
    public List<CartResponse> getUserCart(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream().map(item -> new CartResponse(
                        item.getFood().getId(),
                        item.getFood().getName(),
                        item.getFood().getImageUrl(),
                        item.getFood().getPrice(),
                        item.getVariant() != null ? item.getVariant().getId() : null,  //  variantId
                        item.getVariant() != null ? item.getVariant().getName() : null, //  variantName
                        item.getQuantity(),
                        item.getFood().getSlug()
                )).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncCart(Long userId, List<CartRequest> cartItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));

        for (CartRequest request : cartItems) {
            Food food = foodRepository.findById(request.getFoodId())
                    .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND"));

            CartItem item = cartItemRepository
                    .findByUserIdAndFoodIdAndVariantId(userId, request.getFoodId(), request.getVariantId())
                    .orElse(null);

            if (item != null) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
            } else {
                item = new CartItem();
                item.setUser(user);
                item.setFood(food);
                item.setQuantity(request.getQuantity());

                if (request.getVariantId() != null) {
                    FoodVariant variant = variantRepository.findById(request.getVariantId())
                            .orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND"));
                    item.setVariant(variant);
                }
            }

            cartItemRepository.save(item);
        }
    }

    @Override
    public void addToCart(Long userId, CartRequest request) {
        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND_BY_ID: " + request.getFoodId()));

        CartItem item = cartItemRepository
                .findByUserIdAndFoodIdAndVariantId(userId, request.getFoodId(), request.getVariantId())
                .orElse(new CartItem(userRepository.getReferenceById(userId), food, 0));

        item.setQuantity(item.getQuantity() + request.getQuantity());

        if (request.getVariantId() != null) {
            FoodVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND"));
            item.setVariant(variant);
        }

        cartItemRepository.save(item);
    }

    @Override
    public void updateQuantity(Long userId, CartRequest request) {
        CartItem item = cartItemRepository
                .findByUserIdAndFoodIdAndVariantId(userId, request.getFoodId(), request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND"));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }
    }

    @Override
    public void removeFromCart(Long userId, Long foodId, Long variantId) {
        CartItem item = cartItemRepository
                .findByUserIdAndFoodIdAndVariantId(userId, foodId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND"));
        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

}
