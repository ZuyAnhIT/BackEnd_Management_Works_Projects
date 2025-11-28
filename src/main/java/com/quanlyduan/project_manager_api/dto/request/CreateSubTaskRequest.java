package com.quanlyduan.project_manager_api.dto.request;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSubTaskRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String description;
    private Integer assigneeId;
    private BigDecimal estimatedHours;
}
