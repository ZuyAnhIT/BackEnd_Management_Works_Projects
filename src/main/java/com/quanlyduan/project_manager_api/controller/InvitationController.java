// File: src/main/java/com/quanlyduan/project_manager_api/controller/InvitationController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromProjectInviteRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations") // Endpoint gốc cho mọi loại lời mời
@CrossOrigin("*")
/**
 * Controller xử lý việc xem chi tiết và chấp nhận Lời mời (Company & Project).
 */
public class InvitationController {

    private final CompanyService companyService;
    private final ProjectService projectService;
    private final AuthService authService;

    // CONSTRUCTOR THỦ CÔNG (Gộp 3 Service)
    public InvitationController(CompanyService companyService,
                                 ProjectService projectService,
                                 AuthService authService) {
        this.companyService = companyService;
        this.projectService = projectService;
        this.authService = authService;
    }

    // ========================================================================
    // PHẦN 1: LỜI MỜI CÔNG TY (Company Invitation)
    // ========================================================================

    /**
     * Lấy chi tiết lời mời công ty (Public).
     * Dùng để kiểm tra trạng thái token và user đã tồn tại chưa.
     * URL: /api/invitations/companies/details?token=...
     */
    @GetMapping("/companies/details")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> getCompanyInvitationDetails(
            @RequestParam String token) {

        InvitationDetailsResponse details = companyService.getInvitationDetails(token);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company invitation details retrieved successfully.", details));
    }

    /**
     * Chấp nhận lời mời công ty (User đã đăng nhập).
     * Token được gửi trong Body.
     * URL: /api/invitations/companies/accept
     */
    @PostMapping("/companies/accept")
    public ResponseEntity<ApiResponse<Object>> acceptCompanyInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        companyService.acceptInvitation(request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Successfully accepted company invitation.", null));
    }

    // ========================================================================
    // PHẦN 2: LỜI MỜI DỰ ÁN (Project Invitation)
    // ========================================================================

    /**
     * Lấy chi tiết lời mời dự án (Public).
     * Dùng để kiểm tra trạng thái token và user đã tồn tại chưa (trước khi đăng nhập/đăng ký).
     * URL: /api/invitations/projects/details?token=...
     */
    @GetMapping("/projects/details")
    public ResponseEntity<ApiResponse<ProjectInvitationDetailsResponse>> getProjectInvitationDetails(
            @RequestParam String token) {

        ProjectInvitationDetailsResponse details = projectService.getProjectInvitationDetails(token);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project invitation details retrieved successfully.", details));
    }

    /**
     * Chấp nhận lời mời dự án (User đã đăng nhập).
     * Token được gửi trong Body.
     * URL: /api/invitations/projects/accept
     */
    @PostMapping("/projects/accept")
    public ResponseEntity<ApiResponse<Object>> acceptProjectInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        // Logic service sẽ kiểm tra User đang login và thêm vào Project
        projectService.acceptProjectInvitation(request.getInvitationToken());
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Successfully accepted project invitation.", null));
    }

}