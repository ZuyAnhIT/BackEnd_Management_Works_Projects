// File: src/main/java/com/quanlyduan/project_manager_api/controller/StatisticsController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicProgressResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.RoadmapItemResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
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

    // Cho DỰ ÁN
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getProjectStatistics(
            @PathVariable Integer projectId,

            // 1. Khoảng thời gian (Tùy chọn, mặc định 7 ngày gần nhất nếu null)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            // 2. Bộ lọc nâng cao
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId, // Lọc theo người cụ thể trong dự án
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) List<Integer> statusIds // Lọc theo trạng thái cụ thể
    ) {
        
        // Nếu không gửi ngày, tự động lấy 7 ngày qua
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(7);

        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, null, startDate, endDate, 
            keyword, assigneeId, priority, taskType, statusIds
        );
        
        return ResponseEntity.ok(ApiResponse.success("Project statistics retrieved successfully.", stats));
    }

    // Cho CÁ NHÂN (Tương tự)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMyStatistics(
            @RequestParam(required = false) Integer projectId,
            
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) List<Integer> statusIds
    ) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(7);
        
        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, currentUserId, startDate, endDate,
            keyword, null, priority, taskType, statusIds // assigneeId là currentUserId
        );
        
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

    // ======================================================
    // API TIẾN ĐỘ EPIC (EPIC PROGRESS BAR)
    // ======================================================
    @GetMapping("/projects/{projectId}/epic-progress")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<EpicProgressResponse>>> getEpicProgress(
            @PathVariable Integer projectId,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> statusIds
    ) {
        
        List<EpicProgressResponse> data = statisticsService.getEpicProgress(projectId, sprintId, from, to, statusIds);
        
        return ResponseEntity.ok(ApiResponse.success("Epic progress retrieved successfully.", data));
    }

    @GetMapping("/projects/{projectId}/roadmap")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<RoadmapItemResponse>>> getProjectRoadmap(
            @PathVariable Integer projectId,
            
            @RequestParam(defaultValue = "ALL") String viewType,
            
            // --- Filter Epic ---
            @RequestParam(required = false) List<Integer> epicIds,
            @RequestParam(required = false) List<EpicStatus> epicStatuses,
            
            // --- Filter Sprint (MỚI) ---
            @RequestParam(required = false) List<Integer> sprintIds,
            @RequestParam(required = false) List<SprintStatus> sprintStatuses,
            
            // --- Chung ---
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        
        List<RoadmapItemResponse> roadmap = statisticsService.getProjectRoadmap(
            projectId, viewType, 
            epicIds, epicStatuses, 
            sprintIds, sprintStatuses, 
            keyword, from, to
        );
        
        return ResponseEntity.ok(ApiResponse.success("Project roadmap retrieved successfully.", roadmap));
    }
}