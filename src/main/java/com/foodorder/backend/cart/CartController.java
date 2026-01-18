package com.foodorder.backend.cart;

import com.foodorder.backend.cart.dto.request.CartRequest;
import com.foodorder.backend.cart.dto.response.CartResponse;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý giỏ hàng của người dùng
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API quản lý giỏ hàng - Yêu cầu đăng nhập")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Thêm món vào giỏ", description = "Thêm một món ăn vào giỏ hàng của người dùng đã đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cập nhật số lượng", description = "Cập nhật số lượng của một món trong giỏ hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody CartRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.updateQuantity(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa món khỏi giỏ", description = "Xóa một món ăn khỏi giỏ hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(
            @Parameter(description = "ID của món ăn") @RequestParam Long foodId,
            @Parameter(description = "ID của biến thể (nếu có)") @RequestParam(required = false) Long variantId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.removeFromCart(user.getId(), foodId, variantId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lấy giỏ hàng", description = "Lấy danh sách tất cả các món trong giỏ hàng của người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<List<CartResponse>> getUserCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getUserCart(user.getId()));
    }

    @Operation(summary = "Xóa toàn bộ giỏ hàng", description = "Xóa tất cả các món trong giỏ hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.clearCart(user.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Đồng bộ giỏ hàng", description = "Đồng bộ giỏ hàng từ client lên server (dùng khi đăng nhập).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đồng bộ thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody List<CartRequest> cartItems,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        cartService.syncCart(user.getId(), cartItems);
        return ResponseEntity.ok().build();
    }

}
