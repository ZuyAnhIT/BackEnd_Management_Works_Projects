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
    private String createdByName;  // Tên người tạo (để hiển thị luôn khỏi cần query user)
    private String createdByAvatar; // Avatar (để hiển thị ảnh nhỏ nếu cần)
    private LocalDateTime createdAt; // Ngày tạo
}
