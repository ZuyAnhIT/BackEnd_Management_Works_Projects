// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật trạng thái vòng đời của Không gian làm việc (Workspace).
 * Được sử dụng để thay đổi trạng thái hoạt động, ví dụ: Lưu trữ (ARCHIVE) hoặc Kích hoạt lại (ACTIVE).
 */
@Data
public class UpdateWorkspaceStatusRequest {

    // Trạng thái mới muốn áp dụng (Bắt buộc, phải là giá trị hợp lệ trong Enum WorkspaceStatus)
    // Các giá trị thường dùng: ACTIVE, ARCHIVED, DELETED.
    @NotNull(message = "New status must not be null")
    private WorkspaceStatus newStatus; 
}