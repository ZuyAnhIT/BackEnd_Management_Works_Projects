package com.quanlyduan.project_manager_api.dto.request;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateSubTaskRequest {
    private String title;
    private String description;
    private SubTaskStatus status;
    private Integer assigneeId;
    private BigDecimal estimatedHours;
}
