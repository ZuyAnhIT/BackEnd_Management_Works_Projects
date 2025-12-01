// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/PriorityDistributionResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityDistributionResponse {
    private String priorityName; // Tên hiển thị (Low, Medium...)
    private String priorityCode; // Mã enum (LOW, MEDIUM...)
    private String color;        // Mã màu hex định danh
    private Long taskCount;      // Số lượng
    private Double percentage;   // Phần trăm
}