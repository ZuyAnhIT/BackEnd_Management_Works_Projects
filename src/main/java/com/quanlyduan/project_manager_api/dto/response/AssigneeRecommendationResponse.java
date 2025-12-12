package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssigneeRecommendationResponse {
    private Integer userId;
    private String fullName;
    private String avatarUrl;
    
    // Điểm phù hợp (0 - 100)
    private Double matchScore; 
    
    // Các yếu tố phân tích (AI sẽ dùng cái này để generate văn bản)
    private Integer currentWorkloadPoints; // Số point đang gánh
    private Integer similarTasksCompleted; // Số task tương tự đã làm
    private String workloadStatus;         // "LOW", "OPTIMAL", "HIGH", "OVERLOADED"
    
    // Lý do gợi ý (Human readable text sơ bộ)
    private String reason; 
}