package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    // US7: Tao moi Project trong workspace
    ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request);
    // US8: Danh sach Project theo workspace
    List<ProjectResponse> listProjects(Integer companyId, Integer workspaceId);
    // US8: Chi tiet Project
    ProjectResponse getProject(Integer companyId, Integer workspaceId, Integer projectId);
    // US9: Doi ten Project
    ProjectResponse renameProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request);
    // US9: Xoa mem Project
    void softDeleteProject(Integer companyId, Integer workspaceId, Integer projectId, String reason);
}
