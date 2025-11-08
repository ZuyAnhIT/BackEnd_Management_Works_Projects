package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * US 7: Create Project
 * - POST /api/companies/{companyId}/workspaces/{workspaceId}/projects
 * - Auth: hasWorkspacePermission(workspaceId,'project:create') AND isWorkspaceAdmin(companyId,workspaceId)
 * - Service thực thi IDOR check và kiểm tra trùng mã.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProjectController {

    private final ProjectService projectService;

    /** Tạo dự án trong workspace thuộc company. */
    @PostMapping
    @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'project:create') and @securityService.isWorkspaceAdmin(#companyId, #workspaceId)")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody ProjectRequest request) {

        // Service kiểm tra IDOR, trùng mã, xác định người tạo/manager và ghi DB.
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", created));
    }
}
