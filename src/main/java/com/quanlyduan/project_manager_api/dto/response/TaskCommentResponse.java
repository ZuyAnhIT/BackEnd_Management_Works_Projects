package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskCommentResponse {
    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;
    private CommentUserResponse user;
    private Integer projectId;
    private Integer workspaceId;
    private Integer companyId;
    // ----------------

    @Data
    @Builder
    public static class CommentUserResponse {
        private Integer userId;
        private String fullName;
        private String avatarUrl;
    }
}