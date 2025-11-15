package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<MyWorkspaceResponse>>> getMyWorkspaces() {
        List<MyWorkspaceResponse> workspaces = dashboardService.getMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách workspace của bạn thành công.", workspaces));
    }
}
