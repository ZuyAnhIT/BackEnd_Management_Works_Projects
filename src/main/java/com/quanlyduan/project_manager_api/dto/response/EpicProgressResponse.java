// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/EpicProgressResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EpicProgressResponse {
    // Thông tin Epic
    private Integer epicId;
    private String epicName;
    private String epicCode;
    private String color;

    // KPI: Số lượng Task
    private long totalTasks;
    private long completedTasks;
    private double taskProgressPercent; // (completed / total) * 100

    // KPI: Story Points (Quan trọng cho Agile)
    private long totalPoints;
    private long completedPoints;
    private double pointProgressPercent; // (completedPoints / totalPoints) * 100
    
    // KPI: Thời gian (Optional - Nâng cao)
    // private double totalHours;
    // private double loggedHours;
}