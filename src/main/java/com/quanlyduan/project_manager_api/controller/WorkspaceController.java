package com.quanlyduan.project_manager_api.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.service.WorkspaceService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controller xử lý các nghiệp vụ liên quan đến Không gian làm việc (Workspace) và Thành viên Workspace.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces")
@CrossOrigin("*")
public class WorkspaceController {

    // Khai báo các hằng số mặc định cho phân trang và sắp xếp
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_BY_JOINED_AT = "joinedAt";
    private static final String SORT_DIR_DESC = "desc";

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_CREATE_SUCCESS = "Workspace created successfully.";
    private static final String MSG_LIST_SUCCESS = "Workspace list retrieved successfully.";
    private static final String MSG_SEARCH_SUCCESS = "Workspace search successful.";
    private static final String MSG_DETAIL_SUCCESS = "Workspace details retrieved successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Workspace updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Workspace deleted successfully.";
    private static final String MSG_STATUS_UPDATE_SUCCESS = "Workspace status updated successfully.";
    private static final String MSG_MEMBER_LIST_SUCCESS = "Workspace member list retrieved successfully.";
    private static final String MSG_MEMBER_SEARCH_SUCCESS = "Workspace member search successful.";
    private static final String MSG_MEMBER_DETAIL_SUCCESS = "Workspace member details retrieved successfully.";
    private static final String MSG_MEMBER_INVITE_SUCCESS = "Member successfully added to workspace.";
    private static final String MSG_MEMBER_STATUS_SUCCESS = "Member status updated successfully.";
    private static final String MSG_MEMBER_REMOVE_SUCCESS = "Member successfully removed from workspace.";

    private final WorkspaceService workspaceService;
    private final ObjectMapper objectMapper;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public WorkspaceController(WorkspaceService workspaceService, ObjectMapper objectMapper) {
        this.workspaceService = workspaceService;
        this.objectMapper = objectMapper;
    }

    // ========================================================================
    // QUẢN LÝ KHÔNG GIAN LÀM VIỆC (WORKSPACE CRUD)
    // ========================================================================

    /**
     * Tạo không gian làm việc mới, hỗ trợ tải lên hình ảnh bìa.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:create')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @PathVariable Integer companyId,
            @Parameter(schema = @Schema(implementation = CreateWorkspaceRequest.class))
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        CreateWorkspaceRequest request = parseJson(dataString, CreateWorkspaceRequest.class);
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(companyId, request, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_SUCCESS, newWorkspace));
    }

    /**
     * Lấy danh sách không gian làm việc của công ty theo phân trang.
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(companyId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_LIST_SUCCESS, workspaces));
    }

    /**
     * Tìm kiếm không gian làm việc nâng cao dựa trên tên, mã, mô tả hoặc trạng thái.
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> searchWorkspaces(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) WorkspaceStatus status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<WorkspaceResponse> results = workspaceService.searchWorkspaces(
            companyId, name, code, description, status, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success(MSG_SEARCH_SUCCESS, results));
    }

    /**
     * Lấy thông tin chi tiết của một không gian làm việc cụ thể.
     */
    @GetMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        WorkspaceResponse workspaceDetails = workspaceService.getWorkspaceDetails(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DETAIL_SUCCESS, workspaceDetails));
    }

    /**
     * Cập nhật thông tin không gian làm việc và thay đổi ảnh bìa.
     */
    @PutMapping(value = "/{workspaceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Parameter(schema = @Schema(implementation = UpdateWorkspaceRequest.class))
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UpdateWorkspaceRequest request = parseJson(dataString, UpdateWorkspaceRequest.class);
        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(workspaceId, request, file);

        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, updatedWorkspace));
    }

    /**
     * Xóa mềm không gian làm việc (Chuyển sang trạng thái đã xóa).
     */
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }

    /**
     * Thay đổi trạng thái hoạt động của không gian làm việc.
     */
    @PutMapping("/{workspaceId}/status")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspaceStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceStatusRequest request) {

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspaceStatus(companyId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_STATUS_UPDATE_SUCCESS, updatedWorkspace));
    }

    // ========================================================================
    // QUẢN LÝ THÀNH VIÊN KHÔNG GIAN (MEMBERSHIP)
    // ========================================================================

    /**
     * Lấy danh sách thành viên tham gia vào không gian làm việc.
     */
    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(workspaceId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_LIST_SUCCESS, members));
    }

    /**
     * Tìm kiếm thành viên trong không gian dựa trên tên, email hoặc vai trò.
     */
    @GetMapping("/{workspaceId}/members/search")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> searchWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.searchWorkspaceMembers(
            workspaceId, name, email, role, phone, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_SEARCH_SUCCESS, members));
    }

    /**
     * Lấy thông tin hồ sơ của một thành viên trong không gian.
     */
    @GetMapping("/{workspaceId}/members/{memberId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> getWorkspaceMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {

        WorkspaceMemberResponse memberDetails = workspaceService.getWorkspaceMemberDetails(workspaceId, memberId);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_DETAIL_SUCCESS, memberDetails));
    }

    /**
     * Thêm thành viên của công ty vào không gian làm việc.
     */
    @PostMapping("/{workspaceId}/invite-members")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMemberToWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request) {

        workspaceService.inviteMemberToWorkspace(companyId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_INVITE_SUCCESS, null));
    }

    /**
     * Thay đổi trạng thái (Active/Suspended) của thành viên trong không gian.
     */
    @PutMapping("/{workspaceId}/members/{memberId}/status")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> updateWorkspaceMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {

        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberStatus(companyId, workspaceId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_STATUS_SUCCESS, updatedMember));
    }

    /**
     * Cập nhật vai trò quản trị hoặc nhân viên của thành viên trong không gian.
     */
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')")
    public ResponseEntity<ApiResponse<Object>> updateWorkspaceMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberRole(companyId, workspaceId, memberId, request.getRoleCode());

        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
            updatedMember.getFullName(), updatedMember.getUserId(), updatedMember.getRoleName()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode());
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    /**
     * Loại bỏ hoàn toàn thành viên ra khỏi không gian làm việc.
     */
    @DeleteMapping("/{workspaceId}/members/{memberId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')")
    public ResponseEntity<ApiResponse<Object>> removeWorkspaceMember(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {

        workspaceService.removeMemberFromWorkspace(companyId, workspaceId, memberId);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_REMOVE_SUCCESS, null));
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    /**
     * Chuyển đổi dữ liệu chuỗi JSON nhận được sang đối tượng DTO tương ứng.
     */
    private <T> T parseJson(String dataString, Class<T> clazz) {
        try {
            return objectMapper.readValue(dataString, clazz);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON data format: " + e.getMessage());
        }
    }
}