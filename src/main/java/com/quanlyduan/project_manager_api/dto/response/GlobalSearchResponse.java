package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalSearchResponse {
    
    private List<ProjectSearchResult> projects;
    private List<EpicSearchResult> epics;
    private List<TaskSearchResult> tasks;

    @Data
    @Builder
    public static class ProjectSearchResult {
        private Integer projectId;
        private String projectCode;
        private String projectName;
        private Integer workspaceId; // Mới thêm
        private String workspaceName;
    }

    @Data
    @Builder
    public static class EpicSearchResult {
        private Integer epicId;
        private String epicCode;
        private String epicName;
        private Integer projectId;
        private String projectName;
        private Integer workspaceId; // Mới thêm
    }

    @Data
    @Builder
    public static class TaskSearchResult {
        private Integer taskId;
        private String taskCode;
        private String taskTitle;
        private Integer projectId;
        private String projectName;
        private Integer workspaceId; // Mới thêm
        private String statusName;
        private String statusColor;
    }
}