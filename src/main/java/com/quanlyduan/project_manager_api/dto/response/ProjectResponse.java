package com.quanlyduan.project_manager_api.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Integer id;
    private Integer workspaceId;
    private String name;
    private String projectCode;
    private String description;
    private String coverImageUrl;
    private String goal;
    private Integer projectTypeId;
    private ProjectStatus status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer managerId;
    private Integer createdById;
    private JsonNode boardConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

