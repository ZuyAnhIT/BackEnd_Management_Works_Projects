// File: src/main/java/com/quanlyduan/project_manager_api/controller/CompanyController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.model.Company; // Đã dịch CongTy -> Company
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // API TAO CONG TY
    @PostMapping
    public ResponseEntity<ApiResponse<Company>> createCompany( // Đã dịch CongTy -> Company
            @Valid @RequestBody CreateCompanyRequest request) {

        Company newCompany = companyService.createCompany(request); // Đã dịch CongTy -> Company

        return ResponseEntity
                .status(HttpStatus.CREATED) // Dùng 201 Created cho việc tạo mới
                .body(ApiResponse.success("Company created successfully", newCompany)); // Đã dịch
    }

    // (Thêm các API khác cho Company tại đây: GET, PUT, DELETE, ...)

    // API HIEN THI DANH SACH THANH VIEN CONG TY
    @PreAuthorize("@securityService.isCompanyMember(#congTyId)")
    @GetMapping("/{congTyId}/members")
    public ResponseEntity<ApiResponse<List<CompanyMemberResponse>>> getCompanyMembers(
            @PathVariable Integer congTyId) {

        List<CompanyMemberResponse> members = companyService.getCompanyMembers(congTyId);
        return ResponseEntity.ok(ApiResponse.success("Fetched company members successfully", members)); // Đã dịch
    }

    // API HIEN THI THONG TIN CHI TIET CONG TY
    @PreAuthorize("@securityService.isCompanyMember(#congTyId)")
    @GetMapping("/{congTyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompanyDetails(
            @PathVariable Integer congTyId) {

        CompanyDetailsResponse companyDetails = companyService.getCompanyDetails(congTyId);
        return ResponseEntity.ok(ApiResponse.success("Fetched company details successfully", companyDetails)); // Đã
                                                                                                               // dịch
    }

    // API CAP NHAT THONG TIN CONG TY
    @PreAuthorize("@securityService.isCompanyAdmin(#congTyId)")
    @PutMapping("/{congTyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompany(
            @PathVariable Integer congTyId,
            @Valid @RequestBody UpdateCompanyRequest request) {

        CompanyDetailsResponse updatedCompany = companyService.updateCompany(congTyId, request);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", updatedCompany)); // Đã dịch
    }

    // API MOI THANH VIEN VAO CONG TY
    @PostMapping("/{companyId}/invitations")
    @PreAuthorize("@securityServicePermission.hasCompanyPermission(#companyId, 'company:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer congTyId,
            @Valid @RequestBody InviteMemberRequest request) {

        companyService.inviteMember(congTyId, request);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", null)); // Đã dịch
    }

    /**
     * Sprint 2 - User Story 1: Phân quyền thành viên công ty
     */
    @PutMapping("/{companyId}/members/{memberId}/role")
    @PreAuthorize("@securityServicePermission.hasCompanyPermission(#companyId, 'company:manage_roles')")
    public ResponseEntity<ApiResponse<Object>> updateCompanyMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        // SỬA: Bắt lấy kết quả trả về từ service
        CompanyMember updatedMember = companyService.updateCompanyMemberRole(companyId, memberId, request.getRoleCode());

        // SỬA: Tạo message động
        String message = String.format("Successfully updated role for user %s (ID: %d) to %s",
            updatedMember.getUser().getFullName(),
            updatedMember.getUser().getId(),
            updatedMember.getRole().getRoleCode()
        );

        // SỬA: Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUser().getId());
        responseData.put("fullName", updatedMember.getUser().getFullName());
        responseData.put("newRoleCode", updatedMember.getRole().getRoleCode());

        // SỬA: Trả về message và data mới
        return ResponseEntity.ok(ApiResponse.success(
                message,
                responseData
            ));
    }

}