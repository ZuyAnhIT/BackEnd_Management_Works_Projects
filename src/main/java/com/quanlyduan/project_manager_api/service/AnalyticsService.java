// File: src/main/java/com/quanlyduan/project_manager_api/service/AnalyticsService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectForecastResponse;

import java.util.List;

/**
 * Service chuyên xử lý các logic tính toán, phân tích dữ liệu 
 * để hỗ trợ AI Chatbot hoặc Báo cáo nâng cao.
 */
public interface AnalyticsService {

    /**
     * Phân tích và gợi ý người thực hiện phù hợp nhất cho một task mới.
     * Dựa trên: Mức độ liên quan (Kỹ năng/Lịch sử) và Tải công việc hiện tại.
     *
     * @param projectId ID dự án
     * @param request Thông tin task dự kiến tạo
     * @return Danh sách các ứng viên được xếp hạng theo điểm số phù hợp
     */
    List<AssigneeRecommendationResponse> getAssigneeRecommendations(Integer projectId, AssigneeRecommendationRequest request);

    /**
     * Dự báo tiến độ dự án theo 3 kịch bản: Tốt nhất, Khả thi, Xấu nhất.
     */
    ProjectForecastResponse getProjectForecast(Integer projectId);
    
}