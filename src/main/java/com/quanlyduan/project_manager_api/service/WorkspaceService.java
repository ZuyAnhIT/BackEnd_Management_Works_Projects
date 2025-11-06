// File: src/main/java/com/quanlyduan/project_manager_api/service/WorkspaceService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
// import com.quanlyduan.project_manager_api.model.KhongGian; // Unused import removed

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

    
}