// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkloadResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponse {
    
    // ID thành viên
    private Integer userId; 
    
    // Tên hiển thị trên trục X
    private String userName; 
    
    // Avatar (để hiển thị cho đẹp)
    private String avatarUrl;

    // Tổng tải (chiều cao tổng của cột)
    private double totalLoad; 

    // Các đoạn chồng lên nhau (Stacks)
    private List<WorkloadBreakdown> breakdowns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkloadBreakdown {
        // Tên đoạn (VD: "In Progress" hoặc "High")
        private String stackName; 
        
        // Màu đoạn (VD: #3498db)
        private String color;
        
        // Giá trị của đoạn (Points hoặc Hours)
        private double value;
        
        // Số lượng task trong đoạn này (để hiển thị tooltip: "10h - 3 tasks")
        private int taskCount; 
    }
}