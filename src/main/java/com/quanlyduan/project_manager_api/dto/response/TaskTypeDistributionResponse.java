// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskTypeDistributionResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTypeDistributionResponse {
    private String typeName; // Tên hiển thị (Bug, Story...)
    private String typeCode; // Mã enum (BUG, STORY...)
    private String color;    // Mã màu hex
    private Long taskCount;  // Số lượng
    private Double percentage; // Phần trăm
}