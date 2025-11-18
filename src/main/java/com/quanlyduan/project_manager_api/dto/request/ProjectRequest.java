// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ProjectRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {
    // workspaceId được lấy từ @PathVariable trong Controller, không cần trong body
    // private Integer workspaceId; 
    
    @NotBlank(message = "Tên dự án không được để trống")
    private String name;

    @NotBlank(message = "Mã dự án không được để trống") // Đã dịch
    private String projectCode;

    private String description;
    private String goal;
    private String coverImageUrl;
    private JsonNode boardConfig; // JSON object, sent directly by client

    private Integer projectTypeId; // optional
    private Integer managerId;     // optional

    // Optional planning fields
    private ProjectPriority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
}