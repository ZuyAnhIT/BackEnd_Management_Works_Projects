// File: src/main/java/com/quanlyduan/project_manager_api/controller/WorkspaceController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
// import com.quanlyduan.project_manager_api.model.KhongGian; // Đã được thay thế bằng WorkspaceResponse
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse; 

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces") // Đã dịch
@RequiredArgsConstructor
@CrossOrigin("*")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    // API TAO KHONG GIAN CONG TY
    @PostMapping
    @PreAuthorize("@securityService.isCompanyAdmin(#companyId)") // Đã dịch
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace( // Sửa kiểu trả về
            @PathVariable Integer companyId, // Đã dịch
            @Valid @RequestBody CreateWorkspaceRequest request) {
        
        // Nhận về DTO thay vì Entity
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(companyId, request); // Đã dịch
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", newWorkspace)); // Đã dịch
    }

    // API XEM DANH SACH KHONG GIAN TRONG CONG TY
    @GetMapping
    // Bảo vệ endpoint: Chỉ thành viên công ty (isCompanyMember) mới được xem
    @PreAuthorize("@securityService.isCompanyMember(#companyId)") // Đã dịch
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer companyId) { // Đã dịch
        
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(companyId); // Đã dịch
        
        return ResponseEntity.ok(ApiResponse.success("Fetched workspaces successfully", workspaces)); // Đã dịch
    }


    // API XEM CHI TIET KHONG GIAN CONG TY
    @GetMapping("/{workspaceId}")
    // Bảo vệ endpoint: Yêu cầu là thành viên của không gian này
    @PreAuthorize("@securityService.isWorkspaceMember(#companyId, #workspaceId)") // Đã dịch
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceDetails(
            @PathVariable Integer companyId, // Đã dịch
            @PathVariable Integer workspaceId) {
        
        WorkspaceResponse workspaceDetails = workspaceService.getWorkspaceDetails(workspaceId);
        
        return ResponseEntity.ok(ApiResponse.success("Fetched workspace details successfully", workspaceDetails)); // Đã dịch
    }


    // API THEM THANH VIEN VAO KHONG 
    @PostMapping("/{workspaceId}/invite-members")
    // Bảo vệ: Chỉ WORKSPACE_ADMIN mới được mời
    @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'workspace:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMemberToWorkspace(
            @PathVariable Integer companyId, // Đã dịch
            @PathVariable Integer workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request) {
        
        workspaceService.inviteMemberToWorkspace(companyId, workspaceId, request); // Đã dịch
        
        return ResponseEntity.ok(ApiResponse.success("Member added to workspace successfully", null)); // Đã dịch
    }

    // API CAP NHAT KHONG GIAN
    @PutMapping("/{workspaceId}")
    @PreAuthorize("@securityServicePermission.hasWorkspacePermission(#workspaceId, 'workspace:edit')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(workspaceId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Workspace updated successfully",
                updatedWorkspace
        ));
    }

    // API XOA MEM
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@securityServicePermission.hasCompanyPermission(#companyId, 'workspace:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        workspaceService.deleteWorkspace(workspaceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Workspace deleted successfully",
                null
        ));
    }

}