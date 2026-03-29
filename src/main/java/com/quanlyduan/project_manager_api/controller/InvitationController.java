package com.quanlyduan.project_manager_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.service.ProjectService;

/**
 * Controller xử lý việc xem chi tiết và chấp nhận các loại lời mời (Công ty và Dự án).
 */
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin("*")
public class InvitationController {

    // Khai báo các câu thông báo trả về (Response Messages)
    private static final String MSG_COMPANY_DETAILS_SUCCESS = "Company invitation details retrieved successfully.";
    private static final String MSG_COMPANY_ACCEPT_SUCCESS = "Successfully accepted company invitation.";
    private static final String MSG_PROJECT_DETAILS_SUCCESS = "Project invitation details retrieved successfully.";
    private static final String MSG_PROJECT_ACCEPT_SUCCESS = "Successfully accepted project invitation.";

    private final CompanyService companyService;
    private final ProjectService projectService;
    private final AuthService authService;

    // Khởi tạo thủ công để tiêm (inject) các phụ thuộc cần thiết
    public InvitationController(CompanyService companyService,
                                ProjectService projectService,
                                AuthService authService) {
        this.companyService = companyService;
        this.projectService = projectService;
        this.authService = authService;
    }

    // ========================================================================
    // LỜI MỜI CÔNG TY (COMPANY INVITATION)
    // ========================================================================

    /**
     * Lấy thông tin chi tiết lời mời tham gia công ty (API công khai).
     * Sử dụng để kiểm tra tính hợp lệ của token trước khi người dùng xác nhận.
     */
    @GetMapping("/companies/details")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> getCompanyInvitationDetails(
            @RequestParam String token) {

        InvitationDetailsResponse details = companyService.getInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success(MSG_COMPANY_DETAILS_SUCCESS, details));
    }

    /**
     * Xác nhận chấp nhận lời mời tham gia công ty.
     * Áp dụng cho người dùng đã đăng nhập vào hệ thống.
     */
    @PostMapping("/companies/accept")
    public ResponseEntity<ApiResponse<Object>> acceptCompanyInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        companyService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_COMPANY_ACCEPT_SUCCESS, null));
    }

    // ========================================================================
    // LỜI MỜI DỰ ÁN (PROJECT INVITATION)
    // ========================================================================

    /**
     * Lấy thông tin chi tiết lời mời tham gia dự án (API công khai).
     * Sử dụng để hiển thị thông tin dự án trước khi người dùng quyết định tham gia.
     */
    @GetMapping("/projects/details")
    public ResponseEntity<ApiResponse<ProjectInvitationDetailsResponse>> getProjectInvitationDetails(
            @RequestParam String token) {

        ProjectInvitationDetailsResponse details = projectService.getProjectInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success(MSG_PROJECT_DETAILS_SUCCESS, details));
    }

    /**
     * Xác nhận chấp nhận lời mời tham gia dự án.
     * Hệ thống sẽ tự động gán người dùng hiện tại vào dự án tương ứng.
     */
    @PostMapping("/projects/accept")
    public ResponseEntity<ApiResponse<Object>> acceptProjectInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        projectService.acceptProjectInvitation(request.getInvitationToken());
        return ResponseEntity.ok(ApiResponse.success(MSG_PROJECT_ACCEPT_SUCCESS, null));
    }
}