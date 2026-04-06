package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.dto.response.PersonalDashboardResponse;
import com.quanlyduan.project_manager_api.service.DashboardService;

/**
 * Controller xử lý các nghiệp vụ lấy dữ liệu tổng quan (Dashboard) cho tài khoản cá nhân.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    // Khai báo các câu thông báo trả về
    private static final String MSG_FETCH_WORKSPACES_SUCCESS = "Successfully fetched my workspaces.";
    private static final String MSG_FETCH_COMPANIES_SUCCESS = "Successfully fetched my companies.";
    private static final String MSG_FETCH_PROJECTS_SUCCESS = "Successfully fetched my projects.";
    private static final String MSG_FETCH_TASKS_SUCCESS = "Successfully fetched my tasks.";

    private final DashboardService dashboardService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Lấy danh sách các không gian làm việc (Workspace) mà người dùng đang tham gia.
     */
    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<MyWorkspaceResponse>>> getMyWorkspaces() {
        List<MyWorkspaceResponse> workspaces = dashboardService.getMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_WORKSPACES_SUCCESS, workspaces));
    }

    /**
     * Lấy danh sách các công ty (Company) mà người dùng đang tham gia.
     */
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<MyCompanyResponse>>> getMyCompanies() {
        List<MyCompanyResponse> companies = dashboardService.getMyCompanies();
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_COMPANIES_SUCCESS, companies));
    }

    /**
     * Lấy danh sách các dự án (Project) mà người dùng đang tham gia.
     */
    @GetMapping("/my-projects")
    public ResponseEntity<ApiResponse<List<MyProjectResponse>>> getMyProjects() {
        List<MyProjectResponse> projects = dashboardService.getMyProjects();
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_PROJECTS_SUCCESS, projects));
    }

    /**
     * Lấy danh sách các công việc (Task) chưa hoàn thành được giao trực tiếp cho người dùng.
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MyTaskResponse>>> getMyTasks() {
        List<MyTaskResponse> tasks = dashboardService.getMyTasks();
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_TASKS_SUCCESS, tasks));
    }

    @GetMapping("/my-task-board")
    public ResponseEntity<ApiResponse<PersonalDashboardResponse>> getMyTaskDashboard() {
        PersonalDashboardResponse data = dashboardService.getMyTaskDashboard();
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved personal task board", data));
    }
}