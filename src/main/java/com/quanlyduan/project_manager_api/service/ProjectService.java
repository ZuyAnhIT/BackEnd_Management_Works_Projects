package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;

/** Service US 7: Create Project — thực thi luồng kiểm tra IDOR, trùng mã, người tạo/manager và ghi DB. */
public interface ProjectService {

    /**
     * Tạo dự án mới trong workspace thuộc company.
     * Yêu cầu phân quyền: chỉ quản lý dự án trở lên (thực thi qua @PreAuthorize tại Controller
     * và có thể bổ sung kiểm tra tại Service nếu cần).
     */
    ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request);
}
