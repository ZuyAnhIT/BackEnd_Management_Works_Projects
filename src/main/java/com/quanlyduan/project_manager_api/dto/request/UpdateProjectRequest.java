package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must be at most 255 characters")
    private String name;
}


