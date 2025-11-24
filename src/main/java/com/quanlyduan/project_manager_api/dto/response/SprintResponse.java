// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SprintResponse.java
// (MỚI) DTO cho Sprint (US-S3-6, S3-8, S3-9)
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SprintResponse {
    private Integer id;
    private String name;
    private String goal;
    private String status; // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer projectId;

    private List<TaskResponse> tasks;
}
