//package com.foodorder.backend.food.repository;
//
//import com.foodorder.backend.food.entity.Food;
//import com.foodorder.backend.food.entity.FoodStatus;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
///**
//     * Cập nhật totalSold của món ăn khi đơn hàng hoàn thành
//     * @param foodId ID của món ăn
//     * @param quantity Số lượng cần tăng thêm
//     */
//    @Modifying
//    @Query("UPDATE Food f SET f.totalSold = COALESCE(f.totalSold, 0) + :quantity WHERE f.id = :foodId")
//    void incrementTotalSold(@Param("foodId") Long foodId, @Param("quantity") Integer quantity);
