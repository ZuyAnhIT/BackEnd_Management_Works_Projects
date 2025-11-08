package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {

    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;

    // Thông tin cơ bản của người bình luận
    private CommentUserResponse user;

    // Danh sách người được mention
    private List<CommentUserResponse> mentionedUsers;

    /**
     * Lớp nội bộ (nested class) để chứa thông tin user cơ bản.
     * Chúng ta không muốn trả về toàn bộ Entity User (có mật khẩu, v.v.)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentUserResponse {
        private Integer userId;
        private String fullName;
        private String avatarUrl;
    }
}