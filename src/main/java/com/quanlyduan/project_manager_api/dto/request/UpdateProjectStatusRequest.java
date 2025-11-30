// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateProjectStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật trạng thái vòng đời của Dự án (Project Lifecycle).
 * Dùng để chuyển đổi trạng thái tổng thể của dự án.
 * Ví dụ: NEW (Mới) -> IN_PROGRESS (Đang chạy) -> COMPLETED (Hoàn thành) hoặc PAUSED/CANCELLED.
 */
@Data
public class UpdateProjectStatusRequest {

    // Trạng thái mới muốn áp dụng (Bắt buộc, phải là giá trị hợp lệ trong Enum ProjectStatus)
    @NotNull(message = "New status must not be null")
    private ProjectStatus newStatus;
}