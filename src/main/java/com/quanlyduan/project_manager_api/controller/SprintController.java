// File: src/main/java/com/quanlyduan/project_manager_api/controller/SprintController.java
// (MỚI) Controller cho Sprint
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.*;
import com.quanlyduan.project_manager_api.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api") // Dùng root /api
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    /**
     * US-S3-6: Tạo Sprint mới (trong 1 project)
     */
    @PostMapping("/projects/{projectId}/sprints")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:create')")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateSprintRequest request) {
        
        // Gán projectId từ Path vào DTO
        // request.setProjectId(projectId);
        SprintResponse sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sprint created", sprint));
    }

      /**
     * US-S3-8: Bắt đầu một Sprint
     */
    @PostMapping("/projects/{projectId}/sprints/{sprintId}/start") 
    // Annotation MỚI: An toàn, giống hệt createSprint
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')") 
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable Integer projectId, // <-- Thêm projectId
            @PathVariable Integer sprintId) {
        
        // Gọi service với cả hai ID
        SprintResponse sprint = sprintService.startSprint(projectId, sprintId); 
        return ResponseEntity.ok(ApiResponse.success("Sprint started", sprint));
    }

    @GetMapping("/projects/{projectId}/sprints")
    // Dùng quyền 'project:view' (quyền cơ bản nhất để xem project)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsByProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String status) {
        
        List<SprintResponse> sprints = sprintService.getSprintsByProject(projectId, status);
        return ResponseEntity.ok(ApiResponse.success("Sprints retrieved", sprints));
    }
    // === ENDPOINT MỚI ===
    /**
     * US-S3-XX: Hoàn thành một Sprint
     */
    @PostMapping("/projects/{projectId}/sprints/{sprintId}/complete")
    // Dùng chung quyền 'sprint:start' (hoặc 'sprint:edit' nếu bạn muốn)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:start')")
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {
        
        SprintResponse sprint = sprintService.completeSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint completed", sprint));
    }
    // === ENDPOINT MỚI ===
    /**
     * US-S3-XX: Hủy một Sprint
     */
    @PostMapping("/projects/{projectId}/sprints/{sprintId}/cancel")
    // Giả định quyền hủy sprint là 'sprint:delete' (bạn có thể đổi thành 'sprint:edit')
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:delete')")
    public ResponseEntity<ApiResponse<SprintResponse>> cancelSprint(
            @PathVariable Integer projectId,
            @PathVariable Integer sprintId) {
        
        SprintResponse sprint = sprintService.cancelSprint(projectId, sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint cancelled", sprint));
    }
@PreAuthorize("@securityService.hasProjectPermission(sprintService.getProjectIdBySprint(#sprintId), 'project:view')")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprintDetails(
            @PathVariable Integer sprintId) {
        
        SprintResponse sprint = sprintService.getSprintDetails(sprintId);
        return ResponseEntity.ok(ApiResponse.success("Sprint details retrieved", sprint));
    }
}
