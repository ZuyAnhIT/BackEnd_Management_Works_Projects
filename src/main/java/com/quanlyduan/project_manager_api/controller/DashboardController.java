package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse; // Import DTO mới
import com.quanlyduan.project_manager_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard") // (URL chung cho Dashboard)
@CrossOrigin("*")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<MyCompanyResponse>>> getMyCompanies() {
        List<MyCompanyResponse> companies = dashboardService.getMyCompanies();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công ty của bạn thành công.", companies));
    }
}
    // (Các API khác của DashboardController có thể đã có ở đây)

    /**
     * BỔ SUNG CHO US-S3-3:
     * API Xem danh sách Project của tôi.
     * Bảo mật: Đã được bảo mật bằng 'anyRequest().authenticated()' trong SecurityConfig.
     * API này tự lọc theo currentUserId, không cần @PreAuthorize.
     */
    @GetMapping("/my-projects")
    public ResponseEntity<ApiResponse<List<MyProjectResponse>>> getMyProjects() {

        List<MyProjectResponse> projects = dashboardService.getMyProjects();

        return ResponseEntity.ok(ApiResponse.success(
                "Đã tìm nạp thành công dự án của tôi",
                projects
        ));
    }
}
