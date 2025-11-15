package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;

import java.util.List;

public interface DashboardService {

    /**
     * Lấy danh sách workspace mà người dùng hiện tại được mời tham gia.
     */
    List<MyWorkspaceResponse> getMyWorkspaces();
}
