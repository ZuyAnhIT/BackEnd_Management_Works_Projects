// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectStatusController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.service.ProjectStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/statuses") // API con của Project
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Cấu hình Trạng thái (Cột trên Board) của Dự án.
 */
public class ProjectStatusController {

    private final ProjectStatusService projectStatusService;

    // CONSTRUCTOR THỦ CÔNG
    public ProjectStatusController(ProjectStatusService projectStatusService) {
        this.projectStatusService = projectStatusService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH TRẠNG THÁI (LIST STATUSES)
    // ======================================================
    @GetMapping
    // Bảo vệ: Yêu cầu quyền 'project:view'
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<ProjectStatusResponse>>> getStatuses(
            @PathVariable Integer projectId) {

        List<ProjectStatusResponse> response = projectStatusService.getProjectStatuses(projectId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Status list retrieved successfully.", response));
    }

    // ======================================================
    // 2. TẠO TRẠNG THÁI MỚI (CREATE STATUS)
    // ======================================================
    @PostMapping
    // Bảo vệ: Yêu cầu quyền 'project:edit' (Chỉ Admin/Project Admin mới được thêm cột)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectStatusResponse>> createStatus(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateProjectStatusRequest request) {

        ProjectStatusResponse response = projectStatusService.createStatus(projectId, request);

        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("New status created successfully.", response));
    }

    // ======================================================
    // 3. CẬP NHẬT THÔNG TIN TRẠNG THÁI (UPDATE INFO)
    // ======================================================
    @PutMapping("/{statusId}")
    // Bảo vệ: Cần quyền 'project:edit'
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectStatusResponse>> updateStatus(
            @PathVariable Integer projectId,
            @PathVariable Integer statusId,
            @Valid @RequestBody UpdateStatusRequest request) {

        ProjectStatusResponse response = projectStatusService.updateStatus(projectId, statusId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully.", response));
    }

    // ======================================================
    // 4. SẮP XẾP LẠI THỨ TỰ CỘT (REORDER STATUSES)
    // ======================================================
    @PutMapping("/reorder")
    // Bảo vệ: Cần quyền 'project:edit' (Sửa dự án/cấu trúc bảng)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Object>> reorderStatuses(
            @PathVariable Integer projectId,
            @Valid @RequestBody ReorderStatusRequest request) {

        projectStatusService.reorderStatuses(projectId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Status order updated successfully.", null));
    }

    // ======================================================
    // 5. XÓA TRẠNG THÁI (DELETE STATUS / COLUMN)
    // ======================================================
    @DeleteMapping("/{statusId}")
    // Bảo vệ: Cần quyền 'project:edit' (Sửa dự án/cấu trúc bảng)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Object>> deleteStatus(
            @PathVariable Integer projectId,
            @PathVariable Integer statusId) {

        // Logic service sẽ kiểm tra ràng buộc (Task) và xóa
        projectStatusService.deleteStatus(projectId, statusId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Status deleted successfully.", null));
    }

}