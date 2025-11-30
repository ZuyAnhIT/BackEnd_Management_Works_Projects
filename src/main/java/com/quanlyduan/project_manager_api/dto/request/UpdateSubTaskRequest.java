// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateSubTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin SubTask (Công việc phụ).
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 */
@Data

public class UpdateSubTaskRequest {
    
    // Tiêu đề mới (Tùy chọn)
    @Schema(description = "New title of the subtask", nullable = true)
    private String title;

    // Mô tả chi tiết mới (Tùy chọn)
    @Schema(description = "New detailed description", nullable = true)
    private String description;

    // Trạng thái mới (Tùy chọn - Enum: TO_DO, IN_PROGRESS, DONE)
    @Schema(description = "New status of the subtask", nullable = true)
    private SubTaskStatus status;

    // ID người thực hiện mới (Tùy chọn)
    @Schema(description = "New assignee ID (User ID)", nullable = true)
    private Integer assigneeId;

    // Thời gian ước tính mới (Tùy chọn)
    @Schema(description = "New estimated hours to complete", nullable = true)
    private BigDecimal estimatedHours;
}