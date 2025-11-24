// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SprintDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SprintDetailsResponse {
    private Integer id;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer projectId;

    private List<TaskSummaryResponse> tasks; 
}