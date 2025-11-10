// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWorkspaceStatusRequest {

    @NotNull(message = "New status must not be null") // Đã dịch
    private WorkspaceStatus newStatus; // (Phải là ACTIVE, ARCHIVED, hoặc DELETED)
}