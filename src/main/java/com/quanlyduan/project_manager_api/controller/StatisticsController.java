// File: src/main/java/com/quanlyduan/project_manager_api/controller/StatisticsController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.impl.StatisticsServiceImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

    // ======================================================
    // API THỐNG KÊ CHUNG (WEEKLY STATISTICS)
    // ======================================================

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

    // ======================================================
    // API BIỂU ĐỒ PHÂN BỐ TRẠNG THÁI (PIE CHART DATA)
    // ======================================================
    
    // 1. Cho DỰ ÁN
    @GetMapping("/projects/{projectId}/status-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<StatusDistributionResponse>>> getProjectStatusDistribution(
            @PathVariable Integer projectId) {
        
        List<StatusDistributionResponse> data = statisticsService.getTaskStatusDistribution(projectId, null);
        
        // Sửa thông báo sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Status distribution retrieved successfully.", data));
    }

    // 2. Cho CÁ NHÂN
    @GetMapping("/me/status-distribution")
    public ResponseEntity<ApiResponse<List<StatusDistributionResponse>>> getMyStatusDistribution(
            @RequestParam(required = false) Integer projectId) { // Optional: Lọc theo project cụ thể của tôi
        
        Integer currentUserId = securityService.getCurrentUserId();
        
        List<StatusDistributionResponse> data = statisticsService.getTaskStatusDistribution(projectId, currentUserId);
        
        // Sửa thông báo sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Personal status distribution retrieved successfully.", data));
    }

    //======================================================
    // API BIỂU ĐỒ PHÂN BỐ ĐỘ ƯU TIÊN (PRIORITY CHART)
    // ======================================================

    // 1. Cho DỰ ÁN
    @GetMapping("/projects/{projectId}/priority-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<PriorityDistributionResponse>>> getProjectPriorityDistribution(
            @PathVariable Integer projectId) {
        
        List<PriorityDistributionResponse> data = statisticsService.getTaskPriorityDistribution(projectId, null);
        
        return ResponseEntity.ok(ApiResponse.success("Priority distribution retrieved successfully.", data));
    }

    // 2. Cho CÁ NHÂN
    @GetMapping("/me/priority-distribution")
    public ResponseEntity<ApiResponse<List<PriorityDistributionResponse>>> getMyPriorityDistribution(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        List<PriorityDistributionResponse> data = statisticsService.getTaskPriorityDistribution(projectId, currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("Personal priority distribution retrieved successfully.", data));
    }


    // ======================================================
    // API BIỂU ĐỒ PHÂN BỐ LOẠI CÔNG VIỆC (TYPE CHART)
    // ======================================================

    // 1. Cho DỰ ÁN
    @GetMapping("/projects/{projectId}/type-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TaskTypeDistributionResponse>>> getProjectTypeDistribution(
            @PathVariable Integer projectId) {
        
        List<TaskTypeDistributionResponse> data = statisticsService.getTaskTypeDistribution(projectId, null);
        
        return ResponseEntity.ok(ApiResponse.success("Task type distribution retrieved successfully.", data));
    }

    // 2. Cho CÁ NHÂN
    @GetMapping("/me/type-distribution")
    public ResponseEntity<ApiResponse<List<TaskTypeDistributionResponse>>> getMyTypeDistribution(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        List<TaskTypeDistributionResponse> data = statisticsService.getTaskTypeDistribution(projectId, currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("Personal task type distribution retrieved successfully.", data));
    }

    // ======================================================
    // API PHÂN BỔ CÔNG VIỆC (WORKLOAD - STACKED BAR CHART)
    // ======================================================
    @GetMapping("/projects/{projectId}/workload")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<WorkloadResponse>>> getProjectWorkload(
            @PathVariable Integer projectId,
            
            // View Options
            @RequestParam(defaultValue = "POINTS") String viewType, // POINTS | HOURS
            @RequestParam(defaultValue = "STATUS") String groupBy,  // STATUS | PRIORITY
            
            // Filters
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> statusIds
    ) {
        
        List<WorkloadResponse> workload = statisticsService.getWorkloadDistribution(
            projectId, viewType, groupBy, sprintId, from, to, statusIds
        );
        
        return ResponseEntity.ok(ApiResponse.success("Workload distribution retrieved successfully.", workload));
    }
}