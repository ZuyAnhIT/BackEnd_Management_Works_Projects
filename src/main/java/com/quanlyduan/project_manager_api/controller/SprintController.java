package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import com.quanlyduan.project_manager_api.service.SprintService;

/**
 * Controller xử lý các nghiệp vụ liên quan đến quản lý Sprint (chu kỳ làm việc) trong Dự án.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@CrossOrigin("*")
public class SprintController {

    // Khai báo các câu thông báo trả về (Response Messages)
    private static final String MSG_CREATE_SUCCESS = "Sprint created successfully.";
    private static final String MSG_START_SUCCESS = "Sprint started successfully.";
    private static final String MSG_COMPLETE_SUCCESS = "Sprint completed successfully.";
    private static final String MSG_FETCH_LIST_SUCCESS = "Sprint list retrieved successfully.";
    private static final String MSG_FETCH_DETAIL_SUCCESS = "Sprint details retrieved successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Sprint information updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Sprint deleted successfully.";

    private final SprintService sprintService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    /**
     * Tạo một Sprint mới cho dự án.
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:create')")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateSprintRequest request) {

        SprintResponse sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_SUCCESS, sprint));
    }

    /**
     * Lấy danh sách tất cả các Sprint thuộc dự án (có hỗ trợ lọc theo trạng thái).
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsByProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String status) {

        List<SprintResponse> sprints = sprintService.getSprintsByProject(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_LIST_SUCCESS, sprints));
    }

    /**
     * Xem thông tin chi tiết của một Sprint cụ thể, bao gồm danh sách các công việc bên trong.
     */
    @GetMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<SprintDetailsResponse>> getSprintDetails(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        SprintDetailsResponse details = sprintService.getSprintDetails(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_DETAIL_SUCCESS, details));
    }

    /**
     * Bắt đầu một Sprint (chuyển trạng thái từ chưa bắt đầu sang đang thực hiện).
     */
    @PostMapping("/{sprintId}/start")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        SprintResponse sprint = sprintService.startSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success(MSG_START_SUCCESS, sprint));
    }

    /**
     * Hoàn thành một Sprint và tự động xử lý các công việc chưa xong (đưa về Backlog).
     */
    @PostMapping("/{sprintId}/complete")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')")
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        SprintResponse sprint = sprintService.completeSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success(MSG_COMPLETE_SUCCESS, sprint));
    }

    /**
     * Cập nhật thông tin cơ bản của Sprint như tên, mục tiêu và thời gian.
     */
    @PutMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:edit')")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId,
            @Valid @RequestBody UpdateSprintRequest request) {

        SprintResponse sprint = sprintService.updateSprint(projectId, sprintId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, sprint));
    }

    /**
     * Xóa một Sprint khỏi dự án. Hệ thống sẽ kiểm tra điều kiện trước khi cho phép xóa hẳn hoặc hủy.
     */
    @DeleteMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        sprintService.deleteSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }
}