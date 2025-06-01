package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.CartRequest;
import com.foodorder.backend.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    List<CartResponse> getUserCart(Long userId);
    void syncCart(Long userId, List<CartRequest> cartItems);

    void addToCart(Long userId, CartRequest request);
    void updateQuantity(Long userId, CartRequest request);
    void removeFromCart(Long userId, Long foodId,  Long variantId);
    void clearCart(Long userId);

}
