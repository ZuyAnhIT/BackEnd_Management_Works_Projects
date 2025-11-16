package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;

import java.util.List;

public interface DashboardService {

    /**
     * Lấy danh sách workspace mà người dùng hiện tại tham gia.
     */
    List<MyWorkspaceResponse> getMyWorkspaces();

    /**
     * Lấy danh sách công ty mà người dùng hiện tại thuộc về.
     */
    List<MyCompanyResponse> getMyCompanies();

    /**
     * Lấy danh sách project mà người dùng hiện tại đang tham gia.
     */
    List<MyProjectResponse> getMyProjects();

    /**
     * Lấy danh sách các task (chưa hoàn thành) được giao cho tôi
     */
    List<MyTaskResponse> getMyTasks();

}
