// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SprintDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SprintDetailsResponse {
    private Integer id;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer projectId;

    // Danh sách các task trong sprint này
    private List<TaskSummaryResponse> tasks; 
}