// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/RoadmapItemResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoadmapItemResponse {
    
    // ID duy nhất cho frontend (VD: "epic-1", "sprint-5")
    private String id; 
    
    // ID gốc trong DB
    private Integer originalId;
    
    // Tên hiển thị (Tên Epic hoặc Tên Sprint)
    private String title;
    
    // Loại: "EPIC" hoặc "SPRINT"
    private String type; 
    
    // Thời gian bắt đầu (Trục X - Start)
    private LocalDateTime startDate;
    
    // Thời gian kết thúc (Trục X - End)
    private LocalDateTime endDate;
    
    // Tiến độ % (Để vẽ phần màu đậm/nhạt trên thanh bar)
    private Double progress; 
    
    // Trạng thái (OPEN, IN_PROGRESS...)
    private String status;
    
    // Màu sắc hiển thị (Hex code)
    private String color;
    
    // Metadata bổ sung (Tooltip)
    private Long totalTasks;
    private Long completedTasks;
}