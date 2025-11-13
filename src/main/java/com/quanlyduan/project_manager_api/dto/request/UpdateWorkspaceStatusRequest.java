// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWorkspaceStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống") // Đã dịch
    private WorkspaceStatus newStatus; // (Phải là ACTIVE, ARCHIVED, hoặc DELETED)
}