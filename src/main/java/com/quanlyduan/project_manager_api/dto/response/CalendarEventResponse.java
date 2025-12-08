// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CalendarEventResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CalendarEventResponse {
    
    // ======================================================
    // 1. CÁC TRƯỜNG CƠ BẢN (STANDARD CALENDAR FIELDS)
    // ======================================================
    
    // ID duy nhất trên giao diện (VD: "task-101", "sprint-5")
    // Giúp phân biệt khi click vào event
    private String id; 
    
    // ID gốc trong Database (101, 5)
    private Integer originalId;
    
    // Tiêu đề hiển thị trên thanh sự kiện
    private String title;
    
    // Thời gian bắt đầu
    private LocalDateTime start;
    
    // Thời gian kết thúc
    private LocalDateTime end;
    
    // Cờ báo hiệu sự kiện kéo dài cả ngày (Sprint thường là true)
    private boolean allDay;

    // ======================================================
    // 2. GIAO DIỆN & PHÂN LOẠI (UI & TYPE)
    // ======================================================
    
    // Loại: "TASK" hoặc "SPRINT"
    private String type; 
    
    // Màu nền (Background)
    private String backgroundColor;
    
    // Màu viền (Border)
    private String borderColor;
    
    // Màu chữ (Text)
    private String textColor;

    // ======================================================
    // 3. THÔNG TIN BỔ SUNG (META DATA CHO TOOLTIP)
    // ======================================================
    
    // Tên trạng thái (VD: "In Progress")
    private String statusName;
    
    // Độ ưu tiên (VD: "URGENT") - Chỉ dùng cho Task
    private String priority;
    
    // Người thực hiện - Chỉ dùng cho Task
    private String assigneeName;
    private String assigneeAvatar;
}