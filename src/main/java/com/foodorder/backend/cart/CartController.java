package com.foodorder.backend.cart;

import com.foodorder.backend.cart.dto.request.CartRequest;
import com.foodorder.backend.cart.dto.response.CartResponse;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody CartRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.updateQuantity(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long foodId,
            @RequestParam(required = false) Long variantId,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.removeFromCart(user.getId(), foodId, variantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CartResponse>> getUserCart(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getUserCart(user.getId()));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.clearCart(user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody List<CartRequest> cartItems,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.syncCart(user.getId(), cartItems);
        return ResponseEntity.ok().build();
    }

}
