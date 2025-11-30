// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskCommentResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Bình luận (Comment) trong Task.
 * Chứa nội dung bình luận, thời gian và thông tin người viết.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {

    // ID định danh của bình luận
    private Integer commentId;

    // Nội dung văn bản của bình luận
    private String content;

    // Thời điểm bình luận được tạo
    private LocalDateTime createdAt;

    // Thông tin người dùng đã viết bình luận này.
    // Sử dụng DTO nội bộ để chỉ lấy những thông tin cần thiết (tránh lộ mật khẩu/email).
    private CommentUserResponse user;

    // ========================================================================
    // STATIC INNER CLASS (DTO NỘI BỘ)
    // ========================================================================

    /**
     * DTO nội bộ (Nested Class) chứa thông tin tóm tắt của người dùng.
     * Được sử dụng để hiển thị avatar và tên người bình luận trên giao diện.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentUserResponse {
        
        // ID người dùng
        private Integer userId;
        
        // Tên hiển thị
        private String fullName;
        
        // Đường dẫn ảnh đại diện
        private String avatarUrl;
    }
}