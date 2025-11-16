package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.service.DashboardService;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    // Constructor tiêm thủ công
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ============================
    // 1. Lấy danh sách Workspace
    // ============================
    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<MyWorkspaceResponse>>> getMyWorkspaces() {
        List<MyWorkspaceResponse> workspaces = dashboardService.getMyWorkspaces();
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách workspace thành công.", workspaces)
        );
    }

    // ============================
    // 2. Lấy danh sách Company
    // ============================
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<MyCompanyResponse>>> getMyCompanies() {
        List<MyCompanyResponse> companies = dashboardService.getMyCompanies();
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách công ty thành công.", companies)
        );
    }

    // ============================
    // 3. Lấy danh sách Project
    // ============================
    @GetMapping("/my-projects")
    public ResponseEntity<ApiResponse<List<MyProjectResponse>>> getMyProjects() {
        List<MyProjectResponse> projects = dashboardService.getMyProjects();
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách dự án thành công.", projects)
        );
    }

     // US4-sprin3: Lấy danh sách các task (chưa hoàn thành) được giao cho tôi
    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()") // Chỉ cần đăng nhập
    public ResponseEntity<ApiResponse<List<MyTaskResponse>>> getMyTasks() {
        List<MyTaskResponse> tasks = dashboardService.getMyTasks();
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched my tasks successfully",
                tasks
        ));
    }
}
