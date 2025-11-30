// File: src/main/java/com/quanlyduan/project_manager_api/controller/SprintController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.*;
import com.quanlyduan.project_manager_api.service.SprintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
// Đặt RequestMapping về /api/projects/{projectId}/sprints
@RequestMapping("/api/projects/{projectId}/sprints")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Sprint trong Dự án.
 */
public class SprintController {

    private final SprintService sprintService;

    // CONSTRUCTOR THỦ CÔNG
    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    // ======================================================
    // 1. TẠO SPRINT MỚI (CREATE SPRINT)
    // ======================================================
    /**
     * Tạo Sprint mới (trong 1 project).
     * Endpoint: POST /api/projects/{projectId}/sprints
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:create')")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateSprintRequest request) {

        SprintResponse sprint = sprintService.createSprint(projectId, request);
        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sprint created successfully.", sprint));
    }

    // ======================================================
    // 2. BẮT ĐẦU SPRINT (START SPRINT)
    // ======================================================
    /**
     * Bắt đầu một Sprint (chuyển trạng thái từ NOT_STARTED sang IN_PROGRESS).
     * Endpoint: POST /api/projects/{projectId}/sprints/{sprintId}/start
     */
    @PostMapping("/{sprintId}/start")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        SprintResponse sprint = sprintService.startSprint(projectId, sprintId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint started successfully.", sprint));
    }

    // ======================================================
    // 3. HOÀN THÀNH SPRINT (COMPLETE SPRINT)
    // ======================================================
    /**
     * Hoàn thành một Sprint (chuyển trạng thái sang COMPLETED).
     * Logic service sẽ tự đẩy Task chưa xong về Backlog.
     * Endpoint: POST /api/projects/{projectId}/sprints/{sprintId}/complete
     */
    @PostMapping("/{sprintId}/complete")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')") // Giả định dùng chung quyền
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        SprintResponse sprint = sprintService.completeSprint(projectId, sprintId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint completed successfully.", sprint));
    }

    // ======================================================
    // 4. LẤY DANH SÁCH SPRINT (LIST SPRINTS)
    // ======================================================
    /**
     * Lấy danh sách Sprint của Dự án (có thể lọc theo trạng thái).
     * Endpoint: GET /api/projects/{projectId}/sprints
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsByProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String status) {

        List<SprintResponse> sprints = sprintService.getSprintsByProject(projectId, status);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint list retrieved successfully.", sprints));
    }

    // ======================================================
    // 5. XEM CHI TIẾT SPRINT (GET DETAILS)
    // ======================================================
    /**
     * Lấy chi tiết Sprint (Bao gồm danh sách task trong Sprint đó).
     * Endpoint: GET /api/projects/{projectId}/sprints/{sprintId}
     */
    @GetMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<SprintDetailsResponse>> getSprintDetails(
            @PathVariable Integer projectId, // Giữ lại để kiểm tra IDOR/quyền
            @PathVariable Integer sprintId) {

        SprintDetailsResponse details = sprintService.getSprintDetails(projectId, sprintId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint details retrieved successfully.", details));
    }

    // ======================================================
    // 6. CẬP NHẬT THÔNG TIN SPRINT (UPDATE SPRINT)
    // ======================================================
    /**
     * Cập nhật thông tin (Tên, Mục tiêu, Ngày) của Sprint.
     * Endpoint: PUT /api/projects/{projectId}/sprints/{sprintId}
     */
    @PutMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:edit')")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId,
            @Valid @RequestBody UpdateSprintRequest request) {

        SprintResponse sprint = sprintService.updateSprint(projectId, sprintId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint information updated successfully.", sprint));
    }

    // ======================================================
    // 7. XÓA SPRINT (DELETE SPRINT)
    // ======================================================
    /**
     * Xóa Sprint (Smart Delete: Xóa hẳn nếu NOT_STARTED & không Task; Hủy/Cancel nếu đang chạy/có Task).
     * Endpoint: DELETE /api/projects/{projectId}/sprints/{sprintId}
     */
    @DeleteMapping("/{sprintId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {

        sprintService.deleteSprint(projectId, sprintId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Sprint deleted successfully.", null));
    }
}