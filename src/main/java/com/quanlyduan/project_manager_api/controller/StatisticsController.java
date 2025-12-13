// File: src/main/java/com/quanlyduan/project_manager_api/controller/StatisticsController.java
package com.quanlyduan.project_manager_api.controller;


import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CalendarEventResponse;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    // THỐNG KÊ CHO DỰ ÁN 
    // ======================================================
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getProjectStatistics(
            @PathVariable Integer projectId,

            // Khoảng thời gian (Optional, Default = 7 ngày qua)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            // Các bộ lọc nâng cao
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId, // Lọc theo thành viên cụ thể
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) List<Integer> statusIds
    ) {
        
        // Logic mặc định thời gian nếu không truyền
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(7);

        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, null, startDate, endDate, 
            keyword, assigneeId, priority, taskType, statusIds
        );
        
        return ResponseEntity.ok(ApiResponse.success("Project statistics retrieved successfully.", stats));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMyStatistics(
            @RequestParam(required = false) Integer projectId, // Lọc theo dự án cụ thể của tôi
            
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
        
        // Truyền currentUserId vào tham số assigneeId thứ 2
        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, currentUserId, startDate, endDate,
            keyword, null, priority, taskType, statusIds
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
    // API PHÂN BỔ CÔNG VIỆC (WORKLOAD - CHART & EXPORT)
    // ======================================================
    @GetMapping("/{projectId}/workload")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<?> getProjectWorkload(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "POINTS") String viewType,
            @RequestParam(defaultValue = "STATUS") String groupBy,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> statusIds, // Tham số này có thể bỏ qua trong export header cho gọn hoặc thêm vào nếu muốn
            @RequestParam(defaultValue = "false") boolean export
    ) {
        
        List<WorkloadResponse> workload = statisticsService.getWorkloadDistribution(
            projectId, viewType, groupBy, sprintId, from, to, statusIds
        );
        
        if (export) {
            // --- CẬP NHẬT: TRUYỀN THÊM CÁC BIẾN LỌC VÀO ---
            byte[] excelContent = statisticsService.exportWorkloadDistributionToExcel(
                workload, viewType, groupBy, sprintId, from, to
            );
            
            String fileName = String.format("Workload_%s_%s.xlsx", groupBy, viewType);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);
        } else {
            return ResponseEntity.ok(ApiResponse.success("Success", workload));
        }
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

    // ======================================================
    // API LỊCH DỰ ÁN (CALENDAR VIEW)
    // URL: /api/statistics/projects/{projectId}/calendar
    // ======================================================
    @GetMapping("/projects/{projectId}/calendar")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> getProjectCalendar(
            @PathVariable Integer projectId,
            
            // Thời gian view
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            // Filter Params
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(defaultValue = "true") boolean showSprints
    ) {
        
        List<CalendarEventResponse> events = statisticsService.getProjectCalendar(
             projectId, 
             from, to,
             keyword, assigneeId, priority, taskType,
             showSprints
        );

        return ResponseEntity.ok(ApiResponse.success("Calendar events retrieved successfully.", events));
    }
}