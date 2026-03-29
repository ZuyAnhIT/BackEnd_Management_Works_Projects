package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectForecastResponse;
import com.quanlyduan.project_manager_api.dto.response.StandupReportResponse;
import com.quanlyduan.project_manager_api.service.AnalyticsService;

/**
 * Controller xử lý các nghiệp vụ phân tích dữ liệu, dự báo và AI gợi ý cho dự án.
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin("*")
public class AnalyticsController {

    // Khai báo các câu thông báo trả về
    private static final String MSG_RECOMMEND_SUCCESS = "Assignee recommendations calculated successfully.";
    private static final String MSG_FORECAST_SUCCESS = "Project forecast calculated successfully.";
    private static final String MSG_STANDUP_SUCCESS = "Daily standup data retrieved successfully.";

    private final AnalyticsService analyticsService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Gợi ý người thực hiện công việc (Task) phù hợp nhất dựa trên dữ liệu phân tích.
     * Yêu cầu quyền xem dự án.
     */
    @PostMapping("/projects/{projectId}/recommend-assignee")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<AssigneeRecommendationResponse>>> recommendAssignee(
            @PathVariable Integer projectId,
            @RequestBody AssigneeRecommendationRequest request) {
        
        List<AssigneeRecommendationResponse> recommendations = analyticsService.getAssigneeRecommendations(projectId, request);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_RECOMMEND_SUCCESS, recommendations));
    }

    /**
     * Dự báo tiến độ, hiệu suất và rủi ro của dự án.
     * Yêu cầu quyền xem dự án.
     */
    @GetMapping("/projects/{projectId}/forecast")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectForecastResponse>> getProjectForecast(
            @PathVariable Integer projectId) {
            
        ProjectForecastResponse forecast = analyticsService.getProjectForecast(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_FORECAST_SUCCESS, forecast));
    }

    /**
     * Lấy dữ liệu báo cáo nhanh phục vụ cho các buổi họp hằng ngày (Daily Standup).
     * Yêu cầu quyền xem dự án.
     */
    @GetMapping("/projects/{projectId}/daily-standup")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<StandupReportResponse>> getDailyStandup(
            @PathVariable Integer projectId) {
            
        StandupReportResponse data = analyticsService.getDailyStandupReport(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_STANDUP_SUCCESS, data));
    }
}