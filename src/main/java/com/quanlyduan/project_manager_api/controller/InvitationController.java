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
// import lombok.RequiredArgsConstructor; // Đã xóa
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations") // Endpoint gốc cho mọi loại lời mời
@CrossOrigin("*")
public class InvitationController {

    private final CompanyService companyService;
    private final ProjectService projectService;
    private final AuthService authService;

    // *** CONSTRUCTOR THỦ CÔNG (Gộp 3 Service) ***
    public InvitationController(CompanyService companyService, 
                                ProjectService projectService,
                                AuthService authService) {
        this.companyService = companyService;
        this.projectService = projectService;
        this.authService = authService;
    }

    // ============================================================
    // PHẦN 1: LỜI MỜI CÔNG TY (Company Invitation)
    // ============================================================

    /**
     * Lấy chi tiết lời mời công ty (Public).
     * URL: /api/invitations/companies/details?token=...
     */
    @GetMapping("/companies/details")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> getCompanyInvitationDetails(
            @RequestParam String token) {
        
        InvitationDetailsResponse details = companyService.getInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lời mời công ty thành công.", details));
    }

    /**
     * Chấp nhận lời mời công ty (User đã đăng nhập).
     * URL: /api/invitations/companies/accept
     */
    @PostMapping("/companies/accept")
    public ResponseEntity<ApiResponse<Object>> acceptCompanyInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        
        companyService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận tham gia công ty thành công.", null));
    }

    // ============================================================
    // PHẦN 2: LỜI MỜI DỰ ÁN (Project Invitation)
    // ============================================================

    /**
     * Lấy chi tiết lời mời dự án (Public).
     * URL: /api/invitations/projects/details?token=...
     */
    @GetMapping("/projects/details")
    public ResponseEntity<ApiResponse<ProjectInvitationDetailsResponse>> getProjectInvitationDetails(
            @RequestParam String token) {
        
        ProjectInvitationDetailsResponse details = projectService.getProjectInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lời mời dự án thành công.", details));
    }
    
    /**
     * Chấp nhận lời mời dự án (User đã đăng nhập).
     * URL: /api/invitations/projects/accept
     */
    @PostMapping("/projects/accept")
    public ResponseEntity<ApiResponse<Object>> acceptProjectInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        projectService.acceptProjectInvitation(request.getInvitationToken());
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận tham gia dự án thành công.", null));
    }
    
}