// File: src/main/java/com.quanlyduan.project_manager_api/dto/response/EpicResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; 

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicResponse {
    private Integer id;
    private Integer projectId;
    private String name;
    private String epicCode; 
    private String description;
    private String color;
    private String status;
    
    private java.time.LocalDate startDate;
    private java.time.LocalDate dueDate;
    
    private LocalDateTime createdAt; 
    
    // --- Metrics (Các chỉ số tiến độ) ---
    private Integer totalTasks;
    private Integer tasksCompleted;
    private Double progressPercentage;
}