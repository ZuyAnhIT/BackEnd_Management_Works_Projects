// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskCommentResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List; // Vẫn cần cho lớp nội bộ

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {

    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;

    private CommentUserResponse user;

    // SỬA: XÓA BỎ
    // private List<CommentUserResponse> mentionedUsers;

    /**
     * Lớp nội bộ (nested class) để chứa thông tin user cơ bản.
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