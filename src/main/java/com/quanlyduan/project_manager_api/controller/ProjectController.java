package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.security.SecurityServicePermission;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ProjectController – US7: Tạo Project mới.
 * - Không sửa code cũ; thêm mới controller dùng service đã triển khai.
 * - Bảo mật dùng bean securityServicePermission (đã có trong hệ thống).
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SecurityServicePermission securityServicePermission;

    /**
     * US7 – API tạo Project mới trong Workspace.
     * Quyền truy cập:
     *   @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'project:create')")
     * Nghiệp vụ tóm tắt:
     *   - Nhận ProjectRequest (name, projectCode bắt buộc; các trường khác tùy chọn).
     *   - Lấy user hiện tại từ securityServicePermission để gán createdBy.
     *   - Ủy quyền cho ProjectService xử lý (validate, unique, reference mapping, save).
     * Kết quả:
     *   - 201 Created + ApiResponse<ProjectResponse> chứa thông tin project vừa tạo.
     */
    @PostMapping
    @PreAuthorize("@securityServicePermission.hasPermission('workspace', #workspaceId, 'project:create')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody ProjectRequest request) {

        Integer creatorId = securityServicePermission.getCurrentUserId();
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", created));
    }

    /**
     * US8 – API xem danh sách Project trong một Workspace.
     * Quyền truy cập:
     *   @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'workspace:view')")
     * Nghiệp vụ tóm tắt:
     *   - Xác thực (ở service) rằng workspace thuộc companyId để ngăn truy cập chéo công ty.
     *   - Lấy danh sách dự án, loại bỏ dự án CANCELLED (ẩn dự án đã hủy) và trả về dạng DTO.
     * Kết quả:
     *   - 200 OK + ApiResponse<List<ProjectResponse>>.
     */
    @GetMapping
    @PreAuthorize("@securityServicePermission.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        List<ProjectResponse> data = projectService.listProjectsByWorkspace(companyId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Fetched projects successfully", data));
    }

    /**
     * Project Trash – API xem danh sách dự án bị hủy (CANCELLED) trong một Workspace.
     * Quyền truy cập:
     *   @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'workspace:view')")
     * Nghiệp vụ tóm tắt:
     *   - Xác thực workspace thuộc companyId ở tầng service.
     *   - Trả về chỉ các dự án có status = CANCELLED.
     * Kết quả:
     *   - 200 OK + ApiResponse<List<ProjectResponse>>.
     */
    @GetMapping("/trash")
    @PreAuthorize("@securityServicePermission.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listTrashedProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        List<ProjectResponse> data = projectService.listCancelledProjectsByWorkspace(companyId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Fetched trashed projects successfully", data));
    }

    /**
     * US9 – API xóa (soft delete) Project: chuyển trạng thái dự án sang CANCELLED.
     * Quyền truy cập:
     *   @PreAuthorize("@securityServicePermission.hasProjectPermission(#projectId, 'project:delete')")
     * Nghiệp vụ tóm tắt:
     *   - Xác thực workspace thuộc companyId, và project thuộc workspace (service làm).
     *   - Đặt status = CANCELLED, không xóa cứng.
     * Kết quả:
     *   - 200 OK + ApiResponse null-data với message thành công.
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityServicePermission.hasPermission('workspace', #workspaceId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {

        projectService.deleteProject(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success("Project cancelled successfully", null));
    }
    
    @GetMapping("/{projectId}")
    @PreAuthorize("@securityServicePermission.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        ProjectResponse response = projectService.getProjectDetails(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success("Fetched project details successfully", response));
    }

}
