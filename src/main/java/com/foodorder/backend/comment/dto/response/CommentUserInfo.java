package com.foodorder.backend.comment.dto.response;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO chứa thông tin người dùng bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin người dùng bình luận")
public class CommentUserInfo {

    @Schema(description = "ID người dùng", example = "1")
    private Long id;

    @Schema(description = "Tên hiển thị", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Username", example = "nguyenvana")
    private String username;

    @Schema(description = "URL avatar", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    /**
     * Chuyển đổi từ User Entity sang DTO
     */
    public static CommentUserInfo fromUser(User user) {
        if (user == null) {
            return null;
        }
        return CommentUserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}

