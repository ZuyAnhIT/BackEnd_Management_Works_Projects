package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProjectStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private ProjectStatus newStatus;
}

