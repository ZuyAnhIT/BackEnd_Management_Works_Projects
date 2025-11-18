// File: src/main/java/com/quanlyduan/project_manager_api/service/WorkspaceService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
// import com.quanlyduan.project_manager_api.model.KhongGian; // Unused import removed
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;

public interface WorkspaceService {
    
    /**
     * Tạo một không gian làm việc mới trong công ty.
     * @param congTyId ID của công ty cha
     * @param request DTO chứa thông tin không gian mới
     * @return WorkspaceResponse DTO của không gian vừa tạo
     */
    WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request); // Đã dịch

    
    /**
     * Lấy danh sách tất cả không gian làm việc của một công ty.
     * @param congTyId ID của công ty
     * @return Danh sách WorkspaceResponse DTO
     */
    List<WorkspaceResponse> getWorkspacesByCompany(Integer companyId); // Đã dịch

    /**
     * Lấy thông tin chi tiết của một không gian làm việc.
     * @param workspaceId ID của không gian cần xem
     * @return WorkspaceResponse DTO
     */
    WorkspaceResponse getWorkspaceDetails(Integer workspaceId);

    /**
     * Mời/Thêm một thành viên công ty vào không gian làm việc.
     * @param congTyId ID công ty (để kiểm tra)
     * @param khongGianId ID không gian
     * @param request DTO chứa email và roleId
     */
    void inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request); // Đã dịch

    /**
     * US 16: Cập nhật thông tin chi tiết của một không gian làm việc.
     * @param workspaceId ID của không gian cần cập nhật
     * @param request DTO chứa các thông tin (tùy chọn) cần cập nhật
     * @return WorkspaceResponse DTO của không gian sau khi đã cập nhật
     */
    WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request);

    /**
     * Xóa mềm (Soft Delete) một không gian làm việc.
     * @param workspaceId ID của không gian cần xóa
     */
    void deleteWorkspace(Integer workspaceId);

    /**
     * Lấy danh sách thành viên của một không gian làm việc.
     * @param workspaceId ID của không gian
     * @return Danh sách WorkspaceMemberResponse DTO
     */
    List<WorkspaceMemberResponse> getWorkspaceMembers(Integer workspaceId);

    /**
     * Lấy thông tin chi tiết của một thành viên trong không gian.
     * @param workspaceId ID của không gian (để kiểm tra)
     * @param memberId ID của bản ghi WorkspaceMember
     * @return WorkspaceMemberResponse DTO
     */
    WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId);

    /**
     * Cập nhật trạng thái của thành viên trong không gian (ACTIVE/SUSPENDED).
     * @param companyId ID công ty
     * @param workspaceId ID không gian
     * @param memberId ID của bản ghi WorkspaceMember
     * @param request DTO chứa trạng thái mới
     * @return WorkspaceMemberResponse DTO đã cập nhật
     */
    WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request);


    /**
     * Cập nhật trạng thái của một không gian làm việc (ACTIVE, ARCHIVED, DELETED).
     * @param companyId ID công ty (để kiểm tra)
     * @param workspaceId ID không gian
     * @param request DTO chứa trạng thái mới
     * @return WorkspaceResponse DTO đã cập nhật
     */
    WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request);

    /**
     * Cập nhật vai trò (Role) của một thành viên trong không gian làm việc.
     * @param companyId ID công ty (để kiểm tra)
     * @param workspaceId ID không gian
     * @param memberId ID của bản ghi WorkspaceMember
     * @param newRoleCode Mã vai trò mới (ví dụ: "WORKSPACE_MEMBER")
     * @return WorkspaceMemberResponse DTO đã cập nhật
     */
    WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode);

    void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId);
}