// File: src/main/java/com.quanlyduan.project_manager_api/service/DashboardService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;

import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến trang Tổng quan (Dashboard) cá nhân.
 * Trả về danh sách các tài nguyên mà người dùng hiện tại đang tham gia.
 */
public interface DashboardService {

    // ========================================================================
    // 1. NHÓM DỮ LIỆU TỔ CHỨC (ORGANIZATION DATA)
    // ========================================================================

    /**
     * Lấy danh sách Công ty mà người dùng hiện tại thuộc về.
     * @return Danh sách tóm tắt các Công ty.
     */
    List<MyCompanyResponse> getMyCompanies();

    /**
     * Lấy danh sách Workspace (Phòng ban) mà người dùng hiện tại tham gia.
     * @return Danh sách tóm tắt các Workspace.
     */
    List<MyWorkspaceResponse> getMyWorkspaces();

    /**
     * Lấy danh sách Project mà người dùng hiện tại đang tham gia.
     * @return Danh sách tóm tắt các Project.
     */
    List<MyProjectResponse> getMyProjects();

    // ========================================================================
    // 2. NHÓM DỮ LIỆU CÔNG VIỆC (TASK DATA)
    // ========================================================================

    /**
     * Lấy danh sách các Task (chưa hoàn thành) được giao cho người dùng hiện tại.
     * @return Danh sách Task (MyTaskResponse) để hiển thị trong widget Task của Dashboard.
     */
    List<MyTaskResponse> getMyTasks();
}