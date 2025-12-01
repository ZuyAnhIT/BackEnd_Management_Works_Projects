// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/StatusDistributionResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionResponse {
    private Integer statusId;
    private String statusName;
    private String color; // Mã màu (để vẽ biểu đồ)
    private Long taskCount; // Số lượng task
    private Double percentage; // Phần trăm (Backend tính hoặc Frontend tính đều được)
}