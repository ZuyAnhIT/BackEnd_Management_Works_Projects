// File: src/main/java/com/quanlyduan/project_manager_api/controller/SprintController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.*;
import com.quanlyduan.project_manager_api.service.SprintService;
// import lombok.RequiredArgsConstructor; // Đã xóa
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
// *** SỬA: Đổi RequestMapping về /api/projects/{projectId}/sprints ***
@RequestMapping("/api/projects/{projectId}/sprints")
// @RequiredArgsConstructor // Đã xóa
public class SprintController {

    private final SprintService sprintService;

    // *** THÊM CONSTRUCTOR THỦ CÔNG ***
    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    /**
     * US-S3-6: Tạo Sprint mới (trong 1 project)
     * Endpoint: POST /api/projects/{projectId}/sprints
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:create')")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateSprintRequest request) {
        
        SprintResponse sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo Sprint thành công.", sprint)); // Đã dịch
    }

    /**
     * US-S3-8: Bắt đầu một Sprint
     * Endpoint: POST /api/projects/{projectId}/sprints/{sprintId}/start
     */
    @PostMapping("/{sprintId}/start")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {
        
        SprintResponse sprint = sprintService.startSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint đã bắt đầu.", sprint)); // Đã dịch
    }

    /**
     * API Lấy danh sách Sprint của Dự án
     * Endpoint: GET /api/projects/{projectId}/sprints
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsByProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String status) {
                
        List<SprintResponse> sprints = sprintService.getSprintsByProject(projectId, status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Sprint thành công.", sprints)); // Đã dịch
    }

    /**
     * US-S3-XX: Hoàn thành một Sprint
     * Endpoint: POST /api/projects/{projectId}/sprints/{sprintId}/complete
     */
    @PostMapping("/{sprintId}/complete")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')") // Giả định dùng chung quyền
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {
                
        SprintResponse sprint = sprintService.completeSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint đã hoàn thành.", sprint)); // Đã dịch
    }

    /**
     * US-S3-XX: Hủy một Sprint
     * Endpoint: POST /api/projects/{projectId}/sprints/{sprintId}/cancel
     */
    @PostMapping("/{sprintId}/cancel")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:delete')")
    public ResponseEntity<ApiResponse<SprintResponse>> cancelSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {
                
        SprintResponse sprint = sprintService.cancelSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint đã bị hủy.", sprint)); // Đã dịch
    }

    /**
     * API XEM CHI TIẾT SPRINT
     * (Bao gồm danh sách task trong Sprint đó)
     * Endpoint: GET /api/projects/{projectId}/sprints/{sprintId}
     */
    // *** SỬA: Chuyển API này vào chung luồng ***
    @GetMapping("/{sprintId}")
    // *** SỬA: Đơn giản hóa PreAuthorize vì đã có projectId ***
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<SprintDetailsResponse>> getSprintDetails(
            @PathVariable Integer projectId, // Giữ lại để kiểm tra quyền
            @PathVariable Integer sprintId) {
                
        // (Chúng ta sẽ cần cập nhật SprintService.getSprintDetails để nó nhận cả projectId
        // và kiểm tra xem sprint có thuộc project đó không, để chống lỗi IDOR)
        SprintDetailsResponse details = sprintService.getSprintDetails(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sprint thành công.", details));
    }
}