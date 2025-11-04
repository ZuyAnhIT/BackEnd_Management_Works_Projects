package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.KhongGian;

public interface WorkspaceService {
    
    /**
     * Tạo một không gian làm việc mới trong công ty.
     * @param congTyId ID của công ty cha
     * @param request DTO chứa thông tin không gian mới
     * @return WorkspaceResponse DTO của không gian vừa tạo
     */
    WorkspaceResponse createWorkspace(Integer congTyId, CreateWorkspaceRequest request);

    
    /**
     * Lấy danh sách tất cả không gian làm việc của một công ty.
     * @param congTyId ID của công ty
     * @return Danh sách WorkspaceResponse DTO
     */
    List<WorkspaceResponse> getWorkspacesByCompany(Integer congTyId);

    /**
     * Lấy thông tin chi tiết của một không gian làm việc.
     * @param workspaceId ID của không gian cần xem
     * @return WorkspaceResponse DTO
     */
    WorkspaceResponse getWorkspaceDetails(Integer workspaceId);
}