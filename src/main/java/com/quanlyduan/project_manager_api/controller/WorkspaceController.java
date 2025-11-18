// File: src/main/java/com/quanlyduan/project_manager_api/controller/WorkspaceController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
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

import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse; 

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces") 
@CrossOrigin("*")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    // API TAO KHONG GIAN CONG TY
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:create')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @PathVariable Integer companyId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        
        // Nhận về DTO thay vì Entity
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(companyId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo không gian làm việc thành công.", newWorkspace));
    }

    // API XEM DANH SACH KHONG GIAN TRONG CONG TY
    @GetMapping
    // Bảo vệ endpoint: Chỉ thành viên công ty (isCompanyMember) mới được xem
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer companyId) {
        
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(companyId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách không gian làm việc thành công.", workspaces));
    }


    // API XEM CHI TIET KHONG GIAN CONG TY
    @GetMapping("/{workspaceId}")
    // Bảo vệ endpoint: Yêu cầu là thành viên của không gian này
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {
        
        WorkspaceResponse workspaceDetails = workspaceService.getWorkspaceDetails(workspaceId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết không gian làm việc thành công.", workspaceDetails));
    }


    // API THEM THANH VIEN VAO KHONG 
    @PostMapping("/{workspaceId}/invite-members") // Giữ nguyên tên API của bạn
    // Bảo vệ: Chỉ người có quyền "mời"
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:invite_member')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<Object>> inviteMemberToWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request) {
        
        workspaceService.inviteMemberToWorkspace(companyId, workspaceId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Thêm thành viên vào không gian làm việc thành công.", null));
    }

    // API CAP NHAT KHONG GIAN
    @PutMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(workspaceId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật không gian làm việc thành công.",
                updatedWorkspace
        ));
    }

    // API XOA MEM
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:delete')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<Object>> deleteWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        workspaceService.deleteWorkspace(workspaceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Xóa không gian làm việc thành công.",
                null
        ));
    }

    // API LAY DANH SACH THANH VIEN TRONG KHONG GIAN
    @GetMapping("/{workspaceId}/members")
    // Bảo vệ: Chỉ thành viên của không gian (isWorkspaceMember) mới được xem
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {
        
        List<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(workspaceId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên của không gian làm việc thành công.", members));
    }


    // API XEM CHI TIET THANH VIEN TRONG KHONG GIAN
    @GetMapping("/{workspaceId}/members/{memberId}")
    // Bảo vệ: Chỉ thành viên của không gian (isWorkspaceMember) mới được xem
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> getWorkspaceMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {
        
        WorkspaceMemberResponse memberDetails = workspaceService.getWorkspaceMemberDetails(workspaceId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết thành viên của không gian làm việc thành công.", memberDetails));
    }

    // API CAP NHAT TRANG THAI THANH VIEN KHONG GIAN (ACTIVE/SUSPENDED)
    // *** SỬA QUYỀN: Quyền đúng phải là 'workspace:remove_member' (quản lý thành viên) ***
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')") // Sửa: Dùng @securityService
    @PutMapping("/{workspaceId}/members/{memberId}/status")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> updateWorkspaceMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {
        
        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberStatus(companyId, workspaceId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành viên của không gian làm việc thành công.", updatedMember));
    }

    // API CAP NHAT TRANG THAI KHONG GIAN (ACTIVE/ARCHIVED/DELETED)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")// Tái sử dụng quyền // Sửa: Dùng @securityService
    @PutMapping("/{workspaceId}/status")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspaceStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceStatusRequest request) {
        
        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspaceStatus(companyId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái của không gian làm việc thành công.", updatedWorkspace));
    }
    
    // API CAP NHAT VAI TRO THANH VIEN KHONG GIAN
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')") // Giả định quyền là 'workspace:manage_roles'
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Object>> updateWorkspaceMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) { // Tái sử dụng DTO

        // 1. Gọi service
        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberRole(companyId, workspaceId, memberId, request.getRoleCode());

        // 2. Tạo message động
        String message = String.format("Cập nhật vai trò cho người dùng '%s' (ID: %d) thành '%s' thành công.",
            updatedMember.getFullName(),
            updatedMember.getUserId(),
            updatedMember.getRoleName()
        );

        // 3. Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode()); // Trả về role code
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }
    // API XOA THANH VIEN KHOI WORKSPACE (Soft Delete)
    // SỬA Ở ĐÂY: Bỏ "{companyId}/workspaces/" đi vì class đã định nghĩa rồi
    @DeleteMapping("/{workspaceId}/members/{memberId}") 
    // Bảo vệ: Kiểm tra quyền 'workspace:remove_member'
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')")
    public ResponseEntity<ApiResponse<Object>> removeWorkspaceMember(
            @PathVariable Integer companyId, // Vẫn lấy được từ đường dẫn cha (Class level)
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {
        
        workspaceService.removeMemberFromWorkspace(companyId, workspaceId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success("Xóa thành viên khỏi không gian làm việc thành công.", null));
    }
}