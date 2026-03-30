package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet ve mot Binh luan (Comment) trong Task.
 * Cung cap noi dung trao doi, thong tin nguoi tao va ngu canh phan cap cua du an.
 */
@Getter
@Setter
@Builder
public class TaskCommentResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH & PHAN CAP (IDENTITY & HIERARCHY)
    // ======================================================

    /** ID dinh danh duy nhat cua ban ghi Binh luan. */
    private Integer commentId;

    /** ID cua Du an, Khong gian lam viec va Cong ty chua binh luan nay. */
    private Integer projectId;
    private Integer workspaceId;
    private Integer companyId;

    // ======================================================
    // 2. NOI DUNG BINH LUAN (COMMENT CONTENT)
    // ======================================================

    /** Noi dung chi tiet cua binh luan (co the chua the HTML/Markdown tuy Frontend). */
    private String content;

    /** Thoi diem binh luan duoc dang tai. */
    private LocalDateTime createdAt;

    // ======================================================
    // 3. THONG TIN NGUOI TAO (AUTHOR INFO)
    // ======================================================

    /** Thong tin chi tiet ve nguoi da viet binh luan. */
    private CommentUserResponse user;

    // ======================================================
    // INNER CLASS: CHI TIET NGUOI DUNG (RULE 8)
    // ======================================================

    @Getter
    @Setter
    @Builder
    public static class CommentUserResponse {
        
        /** ID tai khoan cua nguoi dung. */
        private Integer userId;
        
        /** Ho ten hien thi tren giao dien. */
        private String fullName;
        
        /** Duong dan URL den anh dai dien (Avatar) cua nguoi dung. */
        private String avatarUrl;

        // Constructor viet tay cho Inner Class
        public CommentUserResponse() {}

        public CommentUserResponse(Integer userId, String fullName, String avatarUrl) {
            this.userId = userId;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
        }
    }

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public TaskCommentResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public TaskCommentResponse(Integer commentId, Integer projectId, Integer workspaceId, 
                               Integer companyId, String content, LocalDateTime createdAt, 
                               CommentUserResponse user) {
        this.commentId = commentId;
        this.projectId = projectId;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.content = content;
        this.createdAt = createdAt;
        this.user = user;
    }
}