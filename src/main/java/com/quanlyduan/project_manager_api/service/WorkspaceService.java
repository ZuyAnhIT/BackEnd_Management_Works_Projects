// File: src/main/java/com/quanlyduan/project_manager_api/service/WorkspaceService.java
package com.quanlyduan.project_manager_api.service;


import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

public interface WorkspaceService {
    
    // ========================================================================
    // 1. QUẢN LÝ WORKSPACE (CRUD & Lifecycle)
    // ========================================================================
    
    /**
     * Tạo một không gian làm việc mới (hỗ trợ upload ảnh bìa).
     */
    WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request, MultipartFile coverImageFile); 

    /**
     * Lấy danh sách không gian làm việc của công ty (Phân trang & Sắp xếp).
     */
    PageResponseDTO<WorkspaceResponse> getWorkspacesByCompany(Integer companyId, int page, int size, String sortBy, String sortDir);
    
    /**
     * Tìm kiếm không gian làm việc trong công ty theo tiêu chí.
     */
    PageResponseDTO<WorkspaceResponse> searchWorkspaces(
            Integer companyId, 
            String searchName, String searchCode, String searchDescription, WorkspaceStatus searchStatus,
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Lấy thông tin chi tiết của một không gian làm việc.
     */
    WorkspaceResponse getWorkspaceDetails(Integer workspaceId);

    /**
     * Cập nhật thông tin không gian làm việc và ảnh bìa.
     */
    WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request, MultipartFile coverImageFile);

    /**
     * Cập nhật trạng thái của một không gian làm việc (ACTIVE, ARCHIVED, DELETED).
     */
    WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request);

    /**
     * Xóa mềm (Soft Delete) một không gian làm việc.
     */
    void deleteWorkspace(Integer workspaceId);


    // ========================================================================
    // 2. QUẢN LÝ THÀNH VIÊN (MEMBERSHIP & ROLES)
    // ========================================================================

    /**
     * Mời/Thêm một thành viên công ty vào không gian làm việc.
     */
    WorkspaceMember inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request);

    /**
     * Lấy danh sách thành viên của một không gian làm việc (Phân trang & Sắp xếp).
     */
    PageResponseDTO<WorkspaceMemberResponse> getWorkspaceMembers(Integer workspaceId, int page, int size, String sortBy, String sortDir);

    /**
     * Tìm kiếm thành viên trong phòng ban theo tiêu chí (Tên, Email, Role, SĐT).
     */
    PageResponseDTO<WorkspaceMemberResponse> searchWorkspaceMembers(
            Integer workspaceId, 
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Lấy thông tin chi tiết của một thành viên trong không gian.
     */
    WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId);

    /**
     * Cập nhật trạng thái của thành viên trong không gian (Khôi phục/Tạm dừng).
     */
    WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request);

    /**
     * Cập nhật vai trò (Role) của một thành viên trong không gian làm việc.
     */
    WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode);
    
    /**
     * Xóa mềm một thành viên khỏi không gian làm việc.
     */
    void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId);
}