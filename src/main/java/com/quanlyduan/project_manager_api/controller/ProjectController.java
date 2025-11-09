package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.SoftDeleteRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProjectController {

    private final ProjectService projectService;

    // US7: Tao Project moi trong workspace
    // - Quyen: co 'project:create' tai workspace + la admin workspace
    // - Controller chi nhan request va uy quyen cho Service xu ly nghiep vu
    @PostMapping
    @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'project:create') and @securityService.isWorkspaceAdmin(#companyId, #workspaceId)")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", created));
    }

    // US8: Danh sach Project trong workspace
    // - Quyen: COMPANY_ADMIN hoac WORKSPACE_ADMIN
    // - Controller goi Service.listProjects (Service se kiem tra IDOR va chi tra project chua xoa)
    @GetMapping
    @PreAuthorize("@securityService.isCompanyAdmin(#companyId) or @securityService.isWorkspaceAdmin(#companyId, #workspaceId)")
    public ResponseEntity<ApiResponse<java.util.List<ProjectResponse>>> listProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {
        var data = projectService.listProjects(companyId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Fetched projects successfully", data));
    }

    // US8: Chi tiet Project
    // - Quyen: COMPANY_ADMIN | WORKSPACE_ADMIN | PROJECT_ADMIN (duoc xem chi tiet)
    // - Controller goi Service.getProject (Service se kiem tra IDOR va chi tra project chua xoa)
    @GetMapping("/{projectId}")
    @PreAuthorize("@securityService.isCompanyAdmin(#companyId) or @securityService.isWorkspaceAdmin(#companyId, #workspaceId) or @securityServicePermission.hasProjectPermission(#projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        var data = projectService.getProject(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success("Fetched project details successfully", data));
    }

    // US9: Doi ten Project
    // - Quyen: COMPANY_ADMIN | WORKSPACE_ADMIN | PROJECT_ADMIN (project:edit)
    // - Controller goi Service.renameProject (Service se validate name, cap nhat va tra detail)
    @PutMapping("/{projectId}/rename")
    @PreAuthorize("@securityService.isCompanyAdmin(#companyId) or @securityService.isWorkspaceAdmin(#companyId, #workspaceId) or @securityServicePermission.hasProjectPermission(#projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> renameProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        var data = projectService.renameProject(companyId, workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Project renamed successfully", data));
    }

    // US9: Xoa mem Project
    // - Quyen: COMPANY_ADMIN | WORKSPACE_ADMIN | PROJECT_ADMIN (project:delete)
    // - Controller goi Service.softDeleteProject (Service se set deleted_at/deleted_by/ly do)
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityService.isCompanyAdmin(#companyId) or @securityService.isWorkspaceAdmin(#companyId, #workspaceId) or @securityServicePermission.hasProjectPermission(#projectId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> softDeleteProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestBody(required = false) SoftDeleteRequest request) {
        String reason = request != null ? request.getReason() : null;
        projectService.softDeleteProject(companyId, workspaceId, projectId, reason);
        return ResponseEntity.ok(ApiResponse.success("Project soft deleted successfully", null));
    }
}
