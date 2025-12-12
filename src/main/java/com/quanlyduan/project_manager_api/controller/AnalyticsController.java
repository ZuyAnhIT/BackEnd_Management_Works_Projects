// File: src/main/java/com/quanlyduan/project_manager_api/controller/AnalyticsController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectForecastResponse;
import com.quanlyduan.project_manager_api.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin("*") // Cấu hình CORS cho phép Frontend gọi vào
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Constructor Injection
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ======================================================
    // 1. GỢI Ý NGƯỜI THỰC HIỆN (SMART ASSIGNEE RECOMMENDATION)
    // API này giúp AI trả lời câu hỏi: "Task này nên giao cho ai?"
    // ======================================================
    @PostMapping("/projects/{projectId}/recommend-assignee")
    // Kiểm tra quyền: Chỉ cần có quyền VIEW dự án là được sử dụng tính năng này
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<AssigneeRecommendationResponse>>> recommendAssignee(
            @PathVariable Integer projectId,
            @RequestBody AssigneeRecommendationRequest request
    ) {
        
        List<AssigneeRecommendationResponse> recommendations = analyticsService.getAssigneeRecommendations(projectId, request);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Assignee recommendations calculated successfully.", 
                recommendations
        ));
    }

    // ======================================================
    // 2. DỰ BÁO TIẾN ĐỘ & RỦI RO (PROJECT FORECAST)
    // ======================================================
    @GetMapping("/projects/{projectId}/forecast")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectForecastResponse>> getProjectForecast(
            @PathVariable Integer projectId
    ) {
        ProjectForecastResponse forecast = analyticsService.getProjectForecast(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Project forecast calculated successfully.", 
                forecast
        ));
    }

}