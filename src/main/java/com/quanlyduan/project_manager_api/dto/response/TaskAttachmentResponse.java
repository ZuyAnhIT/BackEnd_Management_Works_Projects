package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskAttachmentResponse {
    private Integer id;
    private Integer taskId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private Integer uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    // --- THÊM MỚI ---
    private Integer projectId;
    private Integer workspaceId;
    private Integer companyId;
    // ----------------
}