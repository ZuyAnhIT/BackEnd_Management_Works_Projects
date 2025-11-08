package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** Input US 7 (Create Project): bắt buộc name, projectCode; còn lại tùy chọn. */
@Data
public class ProjectRequest {

    // Tên dự án (bắt buộc theo schema DB)
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must be at most 255 characters")
    private String name;

    // Mã dự án trong phạm vi workspace (bắt buộc, unique theo (project_code, workspace_id))
    @NotBlank(message = "Project code is required")
    @Size(max = 50, message = "Project code must be at most 50 characters")
    private String projectCode;

    // Thông tin mô tả/goal (không bắt buộc)
    @Size(max = 2000, message = "Description is too long")
    private String description;

    @Size(max = 2000, message = "Goal is too long")
    private String goal;

    // Người quản lý dự án (tùy chọn)
    private Integer managerId;

    // Mức ưu tiên (tùy chọn) — mặc định MEDIUM nếu không truyền
    private Priority priority;

    // Thời gian (tùy chọn)
    private LocalDate startDate;
    private LocalDate dueDate;

    // Loại dự án (nullable theo DB)
    private Integer projectTypeId;

    // Ảnh cover (nullable theo DB)
    private String coverImageUrl;

    // Cấu hình board Kanban (JSON, nullable theo DB)
    private JsonNode boardConfig;
}
