// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/StandupReportResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class StandupReportResponse {
    private String sprintName;
    private LocalDate reportDate;
    private List<MemberUpdate> members;

    @Data
    @Builder
    public static class MemberUpdate {
        private String fullName;
        private String avatarUrl;
        
        // Những gì đã làm xong (Hôm qua/24h qua)
        private List<String> completedTasks;
        
        // Những gì đang làm (Hôm nay)
        private List<String> inProgressTasks;
        
        // Những gì dự kiến làm (To Do) - Optional
        private List<String> todoTasks;
    }
}