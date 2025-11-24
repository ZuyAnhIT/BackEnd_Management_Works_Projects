// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectBacklogResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProjectBacklogResponse {
    
    // Phần 1: Danh sách các Sprint đang chạy hoặc chưa bắt đầu (kèm task bên trong)
    private List<SprintDetailsResponse> activeSprints;
    
    // Phần 2: Product Backlog (Danh sách task chưa gán vào Sprint nào)
    private List<TaskSummaryResponse> backlogTasks;

    private int backlogPageNumber;
    private int backlogPageSize;
    private long backlogTotalElements;
    private int backlogTotalPages;
}