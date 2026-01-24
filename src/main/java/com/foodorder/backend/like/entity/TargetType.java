package com.foodorder.backend.like.entity;

/**
 * Enum định nghĩa loại đối tượng được like/share
 * Dùng chung cho cả Like và Share để đảm bảo tính nhất quán
 */
public enum TargetType {
    FOOD,   // Món ăn
    BLOG,   // Bài viết/Tin tức
    MOVIE   // Phim (dành cho tương lai nếu cần mở rộng)
}

