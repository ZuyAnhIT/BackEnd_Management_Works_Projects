// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectForecastResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ProjectForecastResponse {
    // Thông tin cơ bản
    private Integer totalBacklogPoints; // Khối lượng việc còn lại
    private Double averageVelocity;     // Tốc độ trung bình (point/sprint)
    private LocalDate projectDueDate;   // Deadline dự án
    
    // Đánh giá rủi ro tổng quan
    private String riskLevel;           // LOW, MEDIUM, HIGH, CRITICAL
    private String riskMessage;         // Message tóm tắt để AI đọc ngay

    // 3 Kịch bản dự báo
    private ForecastScenario optimistic;  // Kịch bản tốt nhất
    private ForecastScenario likely;      // Kịch bản khả thi nhất
    private ForecastScenario pessimistic; // Kịch bản xấu nhất

    @Data
    @Builder
    public static class ForecastScenario {
        private String name;                // "Optimistic", "Likely", "Pessimistic"
        private Double velocityUsed;        // Velocity dùng để tính
        private Double sprintsNeeded;       // Số sprint cần thêm
        private LocalDate completionDate;   // Ngày xong dự kiến
        private boolean isLate;             // Có trễ deadline không?
        private Integer daysLate;           // Trễ bao nhiêu ngày (nếu có)
    }
}