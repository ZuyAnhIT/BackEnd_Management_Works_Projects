// File: src/main/java/com/quanlyduan/project_manager_api/controller/StatisticsController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.impl.StatisticsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin("*")
public class StatisticsController {

    private final StatisticsServiceImpl statisticsService;
    private final SecurityService securityService;

    public StatisticsController(StatisticsServiceImpl statisticsService, SecurityService securityService) {
        this.statisticsService = statisticsService;
        this.securityService = securityService;
    }

    // 1. Thống kê cho TOÀN DỰ ÁN (Dành cho Manager/Dashboard Dự án)
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getProjectStatistics(
            @PathVariable Integer projectId) {
        
        // assigneeId = null để lấy toàn bộ dự án
        StatisticsResponse stats = statisticsService.getWeeklyStatistics(projectId, null);
        
        return ResponseEntity.ok(ApiResponse.success("Project statistics retrieved successfully.", stats));
    }

    // 2. Thống kê CÁ NHÂN (Dành cho Dashboard cá nhân / My Work)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMyStatistics(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        
        // Nếu projectId được gửi lên, xem thống kê của TÔI trong DỰ ÁN ĐÓ
        // Nếu không gửi projectId, xem thống kê của TÔI trong TẤT CẢ DỰ ÁN
        StatisticsResponse stats = statisticsService.getWeeklyStatistics(projectId, currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("Personal statistics retrieved successfully.", stats));
    }
}