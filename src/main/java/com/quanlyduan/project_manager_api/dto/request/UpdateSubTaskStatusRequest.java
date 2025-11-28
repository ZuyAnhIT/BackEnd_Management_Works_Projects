package com.quanlyduan.project_manager_api.dto.request; // Dev 4
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import lombok.Data;

@Data
public class UpdateSubTaskStatusRequest {
    private SubTaskStatus status;
}
