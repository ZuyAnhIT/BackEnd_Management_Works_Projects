package com.quanlyduan.project_manager_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Project code is required")
    @Size(max = 50, message = "Project code must be at most 50 characters")
    private String projectCode;

    @Size(max = 2000, message = "Description is too long")
    private String description;

    @Size(max = 2000, message = "Goal is too long")
    private String goal;

    private Integer managerId;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer projectTypeId;
    private String coverImageUrl;
    private JsonNode boardConfig;
}

