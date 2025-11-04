package com.quanlyduan.project_manager_api.service;

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
}