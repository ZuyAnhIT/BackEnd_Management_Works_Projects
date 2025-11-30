// File: src/main/java/com/quanlyduan/project_manager_api/controller/DashboardController.java
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
/**
 * Controller xử lý các nghiệp vụ Dashboard/Tổng quan cá nhân.
 */
public class DashboardController {

    private final DashboardService dashboardService;

    // Constructor tiêm thủ công
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH WORKSPACE CỦA TÔI
    // ======================================================
    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<MyWorkspaceResponse>>> getMyWorkspaces() {
        List<MyWorkspaceResponse> workspaces = dashboardService.getMyWorkspaces();
        return ResponseEntity.ok(
                // Sửa thông báo trả về sang tiếng Anh
                ApiResponse.success("Successfully fetched my workspaces.", workspaces)
        );
    }

    // ======================================================
    // 2. LẤY DANH SÁCH COMPANY CỦA TÔI
    // ======================================================
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<MyCompanyResponse>>> getMyCompanies() {
        List<MyCompanyResponse> companies = dashboardService.getMyCompanies();
        return ResponseEntity.ok(
                // Sửa thông báo trả về sang tiếng Anh
                ApiResponse.success("Successfully fetched my companies.", companies)
        );
    }

    // ======================================================
    // 3. LẤY DANH SÁCH PROJECT CỦA TÔI
    // ======================================================
    @GetMapping("/my-projects")
    public ResponseEntity<ApiResponse<List<MyProjectResponse>>> getMyProjects() {
        List<MyProjectResponse> projects = dashboardService.getMyProjects();
        return ResponseEntity.ok(
                // Sửa thông báo trả về sang tiếng Anh
                ApiResponse.success("Successfully fetched my projects.", projects)
        );
    }

    // ======================================================
    // 4. LẤY DANH SÁCH TASK ĐƯỢC GIAO CHO TÔI
    // ======================================================
    // Lấy danh sách các task (chưa hoàn thành) được giao cho tôi
    @GetMapping("/my-tasks")
    @PreAuthorize("isAuthenticated()") // Chỉ cần đăng nhập
    public ResponseEntity<ApiResponse<List<MyTaskResponse>>> getMyTasks() {
        List<MyTaskResponse> tasks = dashboardService.getMyTasks();
        return ResponseEntity.ok(ApiResponse.success(
                // Giữ nguyên thông báo gốc nếu đã là tiếng Anh, hoặc dịch rõ ràng hơn
                "Successfully fetched my tasks.",
                tasks
        ));
    }
}