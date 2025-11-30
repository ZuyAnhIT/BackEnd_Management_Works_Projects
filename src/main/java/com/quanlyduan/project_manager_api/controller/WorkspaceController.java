// File: src/main/java/com/quanlyduan/project_manager_api/controller/WorkspaceController.java
package com.quanlyduan.project_manager_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.service.WorkspaceService;

import io.swagger.v3.oas.annotations.media.Schema;

import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Parameter;

import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

@RestController
// Endpoint cha: /api/companies/{companyId}/workspaces
@RequestMapping("/api/companies/{companyId}/workspaces")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Workspace (Không gian làm việc).
 */
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ObjectMapper objectMapper;

    public WorkspaceController(WorkspaceService workspaceService, ObjectMapper objectMapper) {
        this.workspaceService = workspaceService;
        this.objectMapper = objectMapper;
    }

    // ========================================================================
    // A. QUẢN LÝ WORKSPACE (CRUD)
    // ========================================================================

    // API TẠO KHÔNG GIAN (KÈM UPLOAD ẢNH BÌA)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:create')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @PathVariable Integer companyId,

            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = CreateWorkspaceRequest.class))
            @RequestPart("data") String dataString,

            // Nhận file ảnh (Optional)
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        CreateWorkspaceRequest request;
        try {
            request = objectMapper.readValue(dataString, CreateWorkspaceRequest.class);
        } catch (JsonProcessingException e) {
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        // Gọi Service
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(companyId, request, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                // Sửa thông báo trả về sang tiếng Anh (201 Created)
                .body(ApiResponse.success("Workspace created successfully.", newWorkspace));
    }

    // API LẤY DANH SÁCH WORKSPACE (Cơ bản)
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(companyId, page, size, sortBy, sortDir);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace list retrieved successfully.", workspaces));
    }

    // API TÌM KIẾM WORKSPACE (Nâng cao)
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> searchWorkspaces(
            @PathVariable Integer companyId,

            // Các tham số tìm kiếm
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) WorkspaceStatus status,

            // Các tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        PageResponseDTO<WorkspaceResponse> results = workspaceService.searchWorkspaces(
            companyId, name, code, description, status,
            page, size, sortBy, sortDir
        );

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace search successful.", results));
    }


    // API XEM CHI TIẾT KHÔNG GIAN CÔNG TY
    @GetMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        WorkspaceResponse workspaceDetails = workspaceService.getWorkspaceDetails(workspaceId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace details retrieved successfully.", workspaceDetails));
    }


    // API CẬP NHẬT KHÔNG GIAN (KÈM UPLOAD ẢNH BÌA)
    @PutMapping(value = "/{workspaceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = UpdateWorkspaceRequest.class))
            @RequestPart("data") String dataString,

            // Nhận file ảnh
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        UpdateWorkspaceRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateWorkspaceRequest.class);
        } catch (JsonProcessingException e) {
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(workspaceId, request, file);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
                "Workspace updated successfully.",
                updatedWorkspace
        ));
    }

    // API XÓA MỀM WORKSPACE (DELETED)
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        workspaceService.deleteWorkspace(workspaceId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
                "Workspace deleted successfully.",
                null
        ));
    }

    // API CẬP NHẬT TRẠNG THÁI WORKSPACE (ACTIVE/ARCHIVED/DELETED)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")
    @PutMapping("/{workspaceId}/status")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspaceStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceStatusRequest request) {

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspaceStatus(companyId, workspaceId, request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace status updated successfully.", updatedWorkspace));
    }


    // ========================================================================
    // B. QUẢN LÝ THÀNH VIÊN (MEMBERSHIP)
    // ========================================================================

    // API 1: LẤY DANH SÁCH THÀNH VIÊN (Cơ bản)
    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            // SỬA LỖI: Mặc định sắp xếp theo joinedAt
            @RequestParam(defaultValue = "joinedAt") String sortBy,

            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(workspaceId, page, size, sortBy, sortDir);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace member list retrieved successfully.", members));
    }

    // API 2: TÌM KIẾM THÀNH VIÊN (Nâng cao)
    @GetMapping("/{workspaceId}/members/search")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> searchWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String phone,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            // SỬA LỖI: Mặc định sắp xếp theo joinedAt
            @RequestParam(defaultValue = "joinedAt") String sortBy,

            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.searchWorkspaceMembers(
            workspaceId, name, email, role, phone, page, size, sortBy, sortDir
        );
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace member search successful.", members));
    }


    // API XEM CHI TIẾT THÀNH VIÊN TRONG KHÔNG GIAN
    @GetMapping("/{workspaceId}/members/{memberId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> getWorkspaceMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {

        WorkspaceMemberResponse memberDetails = workspaceService.getWorkspaceMemberDetails(workspaceId, memberId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Workspace member details retrieved successfully.", memberDetails));
    }

    // API MỜI THÀNH VIÊN VÀO KHÔNG GIAN (CHỈ THÀNH VIÊN CÔNG TY)
    @PostMapping("/{workspaceId}/invite-members")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMemberToWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request) {

        workspaceService.inviteMemberToWorkspace(companyId, workspaceId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Member successfully added to workspace.", null));
    }

    // API CẬP NHẬT TRẠNG THÁI THÀNH VIÊN (ACTIVE/SUSPENDED)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')") // Quyền quản lý thành viên
    @PutMapping("/{workspaceId}/members/{memberId}/status")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> updateWorkspaceMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {

        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberStatus(companyId, workspaceId, memberId, request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Member status updated successfully.", updatedMember));
    }

    // API CẬP NHẬT VAI TRÒ THÀNH VIÊN KHÔNG GIAN
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')") // Quyền quản lý vai trò
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Object>> updateWorkspaceMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        // 1. Gọi service
        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberRole(companyId, workspaceId, memberId, request.getRoleCode());

        // 2. Tạo message động (Sửa thông báo trả về sang tiếng Anh)
        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
            updatedMember.getFullName(),
            updatedMember.getUserId(),
            updatedMember.getRoleName()
        );

        // 3. Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode());
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    // API XÓA THÀNH VIÊN KHỎI WORKSPACE (Soft Delete: REMOVED)
    @DeleteMapping("/{workspaceId}/members/{memberId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')") // Quyền xóa thành viên
    public ResponseEntity<ApiResponse<Object>> removeWorkspaceMember(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {

        workspaceService.removeMemberFromWorkspace(companyId, workspaceId, memberId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Member successfully removed from workspace.", null));
    }
}