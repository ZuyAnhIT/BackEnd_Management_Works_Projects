// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/EpicResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Epic (Sử thi/Mục tiêu lớn).
 * Bao gồm cả thông tin cơ bản và các chỉ số tiến độ được tính toán từ các Task con.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================
    
    private Integer id;
    
    // ID của Dự án chứa Epic này
    private Integer projectId;
    
    // Mã định danh Epic (ví dụ: "WEB-E-1")
    private String epicCode; 

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================
    
    private String name;
    private String description;
    
    // Mã màu hiển thị trên giao diện (ví dụ: #8E44AD)
    private String color;
    
    // Trạng thái hiện tại (OPEN, IN_PROGRESS, DONE...)
    private String status;

    // ========================================================================
    // 3. THÔNG TIN THỜI GIAN (TIMELINE)
    // ========================================================================
    
    private LocalDate startDate;
    private LocalDate dueDate;
    
    // Thời điểm tạo Epic
    private LocalDateTime createdAt; 

    // ========================================================================
    // 4. CHỈ SỐ TIẾN ĐỘ (METRICS) - Dữ liệu tính toán
    // ========================================================================
    
    // Tổng số lượng công việc (Task) thuộc Epic này
    private Integer totalTasks;
    
    // Số lượng công việc đã hoàn thành (IsCompleted = true)
    private Integer tasksCompleted;
    
    // Tỷ lệ hoàn thành (0.0 - 100.0). Dùng để vẽ thanh Progress Bar.
    private Double progressPercentage;
}