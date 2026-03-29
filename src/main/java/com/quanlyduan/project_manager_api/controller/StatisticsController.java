package com.quanlyduan.project_manager_api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

/**
 * Controller xử lý các nghiệp vụ thống kê, báo cáo và trích xuất dữ liệu của hệ thống.
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin("*")
public class StatisticsController {

    // Khai báo các hằng số mặc định
    private static final int DEFAULT_DAYS_RANGE = 7;
    private static final String EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_PROJECT_STATS_SUCCESS = "Project statistics retrieved successfully.";
    private static final String MSG_PERSONAL_STATS_SUCCESS = "Personal statistics retrieved successfully.";
    private static final String MSG_STATUS_DIST_SUCCESS = "Status distribution retrieved successfully.";
    private static final String MSG_PRIORITY_DIST_SUCCESS = "Priority distribution retrieved successfully.";
    private static final String MSG_TYPE_DIST_SUCCESS = "Task type distribution retrieved successfully.";
    private static final String MSG_EPIC_PROGRESS_SUCCESS = "Epic progress retrieved successfully.";
    private static final String MSG_ROADMAP_SUCCESS = "Project roadmap retrieved successfully.";
    private static final String MSG_CALENDAR_SUCCESS = "Calendar events retrieved successfully.";

    private final StatisticsServiceImpl statisticsService;
    private final SecurityService securityService;

    // Khởi tạo thủ công để tiêm phụ thuộc
    public StatisticsController(StatisticsServiceImpl statisticsService, SecurityService securityService) {
        this.statisticsService = statisticsService;
        this.securityService = securityService;
    }

    // ======================================================
    // THỐNG KÊ TỔNG QUAN (OVERVIEW)
    // ======================================================

    /**
     * Lấy dữ liệu thống kê tổng quan của một dự án.
     */
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getProjectStatistics(
            @PathVariable Integer projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) List<Integer> statusIds) {
        
        LocalDate endDate = getEffectiveEndDate(to);
        LocalDate startDate = getEffectiveStartDate(from, endDate);

        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, null, startDate, endDate, 
            keyword, assigneeId, priority, taskType, statusIds
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_PROJECT_STATS_SUCCESS, stats));
    }

    /**
     * Lấy dữ liệu thống kê cá nhân của người dùng hiện tại.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMyStatistics(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) List<Integer> statusIds) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        LocalDate endDate = getEffectiveEndDate(to);
        LocalDate startDate = getEffectiveStartDate(from, endDate);
        
        StatisticsResponse stats = statisticsService.getOverviewStatistics(
            projectId, currentUserId, startDate, endDate,
            keyword, null, priority, taskType, statusIds
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_PERSONAL_STATS_SUCCESS, stats));
    }

    // ======================================================
    // BIỂU ĐỒ PHÂN BỐ (DISTRIBUTION CHARTS)
    // ======================================================

    /**
     * Phân bố trạng thái công việc trong dự án.
     */
    @GetMapping("/projects/{projectId}/status-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<StatusDistributionResponse>>> getProjectStatusDistribution(
            @PathVariable Integer projectId) {
        
        List<StatusDistributionResponse> data = statisticsService.getTaskStatusDistribution(projectId, null);
        return ResponseEntity.ok(ApiResponse.success(MSG_STATUS_DIST_SUCCESS, data));
    }

    /**
     * Phân bố trạng thái công việc của cá nhân.
     */
    @GetMapping("/me/status-distribution")
    public ResponseEntity<ApiResponse<List<StatusDistributionResponse>>> getMyStatusDistribution(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        List<StatusDistributionResponse> data = statisticsService.getTaskStatusDistribution(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(MSG_STATUS_DIST_SUCCESS, data));
    }

    /**
     * Phân bố độ ưu tiên công việc trong dự án.
     */
    @GetMapping("/projects/{projectId}/priority-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<PriorityDistributionResponse>>> getProjectPriorityDistribution(
            @PathVariable Integer projectId) {
        
        List<PriorityDistributionResponse> data = statisticsService.getTaskPriorityDistribution(projectId, null);
        return ResponseEntity.ok(ApiResponse.success(MSG_PRIORITY_DIST_SUCCESS, data));
    }

    /**
     * Phân bố độ ưu tiên công việc của cá nhân.
     */
    @GetMapping("/me/priority-distribution")
    public ResponseEntity<ApiResponse<List<PriorityDistributionResponse>>> getMyPriorityDistribution(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        List<PriorityDistributionResponse> data = statisticsService.getTaskPriorityDistribution(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(MSG_PRIORITY_DIST_SUCCESS, data));
    }

    /**
     * Phân bố loại công việc trong dự án.
     */
    @GetMapping("/projects/{projectId}/type-distribution")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TaskTypeDistributionResponse>>> getProjectTypeDistribution(
            @PathVariable Integer projectId) {
        
        List<TaskTypeDistributionResponse> data = statisticsService.getTaskTypeDistribution(projectId, null);
        return ResponseEntity.ok(ApiResponse.success(MSG_TYPE_DIST_SUCCESS, data));
    }

    /**
     * Phân bố loại công việc của cá nhân.
     */
    @GetMapping("/me/type-distribution")
    public ResponseEntity<ApiResponse<List<TaskTypeDistributionResponse>>> getMyTypeDistribution(
            @RequestParam(required = false) Integer projectId) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        List<TaskTypeDistributionResponse> data = statisticsService.getTaskTypeDistribution(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(MSG_TYPE_DIST_SUCCESS, data));
    }

    // ======================================================
    // HIỆU SUẤT & TIẾN ĐỘ (WORKLOAD & PROGRESS)
    // ======================================================

    /**
     * Thống kê khối lượng công việc và hỗ trợ xuất file Excel.
     */
    @GetMapping("/{projectId}/workload")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<?> getProjectWorkload(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "POINTS") String viewType,
            @RequestParam(defaultValue = "STATUS") String groupBy,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> statusIds,
            @RequestParam(defaultValue = "false") boolean export) {
        
        List<WorkloadResponse> workload = statisticsService.getWorkloadDistribution(
            projectId, viewType, groupBy, sprintId, from, to, statusIds
        );
        
        if (export) {
            byte[] excelContent = statisticsService.exportWorkloadDistributionToExcel(
                workload, viewType, groupBy, sprintId, from, to
            );
            String fileName = String.format("Workload_%s_%s.xlsx", groupBy, viewType);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                    .body(excelContent);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Success", workload));
    }

    /**
     * Thống kê tiến độ các Epic và hỗ trợ xuất file Excel.
     */
    @GetMapping("/projects/{projectId}/epic-progress")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<?> getEpicProgress(
            @PathVariable Integer projectId,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> statusIds,
            @RequestParam(defaultValue = "false") boolean export) {
        
        List<EpicProgressResponse> data = statisticsService.getEpicProgress(
            projectId, sprintId, from, to, statusIds
        );

        if (export) {
            byte[] excelContent = statisticsService.exportEpicProgressToExcel(
                data, projectId, sprintId, from, to
            );
            
            String fileName = (sprintId != null) 
                ? "Epic_Progress_Sprint" + sprintId + ".xlsx" 
                : "Epic_Progress_Project" + projectId + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                    .body(excelContent);
        }
        
        return ResponseEntity.ok(ApiResponse.success(MSG_EPIC_PROGRESS_SUCCESS, data));
    }

    // ======================================================
    // LỘ TRÌNH & LỊCH (ROADMAP & CALENDAR)
    // ======================================================

    /**
     * Truy xuất dữ liệu lộ trình dự án (Roadmap) dựa trên Epic và Sprint.
     */
    @GetMapping("/projects/{projectId}/roadmap")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<RoadmapItemResponse>>> getProjectRoadmap(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "ALL") String viewType,
            @RequestParam(required = false) List<Integer> epicIds,
            @RequestParam(required = false) List<EpicStatus> epicStatuses,
            @RequestParam(required = false) List<Integer> sprintIds,
            @RequestParam(required = false) List<SprintStatus> sprintStatuses,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        List<RoadmapItemResponse> roadmap = statisticsService.getProjectRoadmap(
            projectId, viewType, epicIds, epicStatuses, sprintIds, sprintStatuses, keyword, from, to
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_ROADMAP_SUCCESS, roadmap));
    }

    /**
     * Truy xuất danh sách sự kiện lịch của dự án (Công việc và Sprint).
     */
    @GetMapping("/projects/{projectId}/calendar")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<CalendarEventResponse>>> getProjectCalendar(
            @PathVariable Integer projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(defaultValue = "true") boolean showSprints) {
        
        List<CalendarEventResponse> events = statisticsService.getProjectCalendar(
             projectId, from, to, keyword, assigneeId, priority, taskType, showSprints
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_CALENDAR_SUCCESS, events));
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    private LocalDate getEffectiveEndDate(LocalDate to) {
        return (to != null) ? to : LocalDate.now();
    }

    private LocalDate getEffectiveStartDate(LocalDate from, LocalDate endDate) {
        return (from != null) ? from : endDate.minusDays(DEFAULT_DAYS_RANGE);
    }
}