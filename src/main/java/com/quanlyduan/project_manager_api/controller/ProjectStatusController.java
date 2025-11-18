// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectStatusController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
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
public class ProjectStatusController {

    private final ProjectStatusService projectStatusService;

    // *** CONSTRUCTOR THỦ CÔNG ***
    public ProjectStatusController(ProjectStatusService projectStatusService) {
        this.projectStatusService = projectStatusService;
    }

    // API LẤY DANH SÁCH TRẠNG THÁI (HIỂN THỊ BOARD)
    @GetMapping
    // Bảo vệ: Yêu cầu quyền 'project:view' (Thành viên dự án hoặc Admin đều xem được)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')") 
    public ResponseEntity<ApiResponse<List<ProjectStatusResponse>>> getStatuses(
            @PathVariable Integer projectId) {
        
        List<ProjectStatusResponse> response = projectStatusService.getProjectStatuses(projectId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trạng thái thành công.", response));
    }

    // API TAO TRANG THAI MOI
    @PostMapping
    // Bảo vệ: Yêu cầu quyền 'project:edit' (Chỉ Admin/Project Admin mới được thêm cột)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')") 
    public ResponseEntity<ApiResponse<ProjectStatusResponse>> createStatus(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateProjectStatusRequest request) {
        
        ProjectStatusResponse response = projectStatusService.createStatus(projectId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo trạng thái mới thành công.", response)); // Đã dịch
    }
}