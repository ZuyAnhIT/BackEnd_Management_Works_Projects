package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TagResponse {
    private Integer id;
    private String name;
    private String color;
    private String description;
    private Integer projectId;

    // --- Bổ sung thông tin Audit ---
    private Integer createdById;   // ID người tạo
    private String createdByName;  // Tên người tạo
    private String createdByAvatar; // Avatar
    private LocalDateTime createdAt; // Ngày tạo
    private LocalDateTime updatedAt; // Ngày cập nhật (MỚI THÊM)
}