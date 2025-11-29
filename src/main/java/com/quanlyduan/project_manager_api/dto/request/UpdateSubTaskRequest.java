package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Request to update Subtask", example = """
{
  "title": "Updated Subtask Title",
  "description": "Updated description of the subtask",
  "status": "IN_PROGRESS",
  "assigneeId": null,  
    "estimatedHours": 12.5
}
""")
public class UpdateSubTaskRequest {
    
    @Schema(description = "New title", nullable = true)
    private String title;

    @Schema(description = "New description", nullable = true)
    private String description;

    @Schema(description = "New status", nullable = true)
    private SubTaskStatus status;

    @Schema(description = "New assignee ID", nullable = true)
    private Integer assigneeId;

    @Schema(description = "New estimated hours", nullable = true)
    private BigDecimal estimatedHours;
}