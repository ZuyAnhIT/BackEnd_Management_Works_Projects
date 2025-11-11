package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;

public interface ProjectStatusService {
    ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request);
}

