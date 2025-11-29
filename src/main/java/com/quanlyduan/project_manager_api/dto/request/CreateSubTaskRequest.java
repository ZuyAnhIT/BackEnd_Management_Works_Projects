package com.quanlyduan.project_manager_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
// --- GIẢI PHÁP: Định nghĩa mẫu JSON cụ thể ở đây ---
@Schema(description = "Request to create Subtask", example = """
{
  "title": "Database Design",
  "description": "Create ERD and Schema",
  "assigneeId": null,
  "estimatedHours": null
}
""")
public class CreateSubTaskRequest {

    @NotBlank(message = "Title cannot be empty")
    @Schema(description = "Subtask title")
    private String title;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Assignee ID (Leave empty if unassigned)")
    private Integer assigneeId;

    @Schema(description = "Estimated hours")
    private BigDecimal estimatedHours;
}