// File: src/main/java/com/quanlyduan/project_manager_api/controller/CompanyController.java
package com.quanlyduan.project_manager_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Công ty (Company) và Thành viên (CompanyMember).
 */
public class CompanyController {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    public CompanyController(CompanyService companyService, ObjectMapper objectMapper) {
        this.companyService = companyService;
        this.objectMapper = objectMapper;
    }

    // ========================================================================
    // A. QUẢN LÝ CÔNG TY (CRUD)
    // ========================================================================

    // API TẠO CÔNG TY
    @PostMapping
    @PreAuthorize("@securityService.hasSystemPermission('company:create')")
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        Company newCompany = companyService.createCompany(request);

        return ResponseEntity
                // Sửa thông báo trả về sang tiếng Anh
                .status(HttpStatus.CREATED) // Dùng 201 Created cho việc tạo mới
                .body(ApiResponse.success("Company created successfully.", newCompany));
    }

    // API HIỂN THỊ THÔNG TIN CHI TIẾT CÔNG TY
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompanyDetails(
            @PathVariable Integer companyId) {

        CompanyDetailsResponse companyDetails = companyService.getCompanyDetails(companyId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company details retrieved successfully.", companyDetails));
    }

    // API CẬP NHẬT THÔNG TIN CÔNG TY (TICH HOP UPLOAD LOGO)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:edit')")
    @PutMapping(value = "/{companyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompany(
            @PathVariable Integer companyId,

            // Nhận JSON String và convert thủ công
            @Parameter(schema = @Schema(implementation = UpdateCompanyRequest.class)) // Gợi ý cho Swagger
            @RequestPart("data") String dataString,

            // Nhận file ảnh (Optional)
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        UpdateCompanyRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateCompanyRequest.class);
        } catch (JsonProcessingException e) {
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        // Gọi Service (Xử lý file và update DB)
        CompanyDetailsResponse updatedCompany = companyService.updateCompany(companyId, request, file);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company information updated successfully.", updatedCompany));
    }

    // ========================================================================
    // B. QUẢN LÝ THÀNH VIÊN (MEMBERSHIP)
    // ========================================================================

    // API 1: LẤY DANH SÁCH THÀNH VIÊN (Mặc định)
    // URL: GET /api/companies/{id}/members
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    @GetMapping("/{companyId}/members")
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyMemberResponse>>> getCompanyMembers(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<CompanyMemberResponse> members = companyService.getCompanyMembers(companyId, page, size, sortBy, sortDir);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company member list retrieved successfully.", members));
    }

    // API 2: TÌM KIẾM THÀNH VIÊN (Nâng cao)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    @GetMapping("/{companyId}/members/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyMemberResponse>>> searchCompanyMembers(
            @PathVariable Integer companyId,

            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String roleName, // Tên vai trò
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) String phone,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<CompanyMemberResponse> result = companyService.searchCompanyMembers(
            companyId, name, email, jobTitle, roleName, status, phone,
            page, size, sortBy, sortDir
        );
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company member search successful.", result));
    }

    // API XEM CHI TIẾT THÀNH VIÊN
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    @GetMapping("/{companyId}/members/{memberId}")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> getCompanyMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId) {

        CompanyMemberResponse memberDetails = companyService.getCompanyMemberDetails(companyId, memberId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Company member details retrieved successfully.", memberDetails));
    }

    // API CẬP NHẬT TRẠNG THÁI THÀNH VIÊN (ACTIVE/SUSPENDED)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')")
    @PutMapping("/{companyId}/members/{memberId}/status")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> updateMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {

        CompanyMemberResponse updatedMember = companyService.updateMemberStatus(companyId, memberId, request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Member status updated successfully.", updatedMember));
    }

    // API CẬP NHẬT VAI TRÒ (Role) của thành viên
    @PutMapping("/{companyId}/members/{memberId}/role")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')")
    public ResponseEntity<ApiResponse<Object>> updateCompanyMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        // 1. Gọi service và nhận về Entity đã update
        CompanyMember updatedMember = companyService.updateCompanyMemberRole(companyId, memberId, request.getRoleCode());

        // 2. Tạo message động (Sửa thông báo trả về sang tiếng Anh)
        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
            updatedMember.getUser().getFullName(),
            updatedMember.getUser().getId(),
            updatedMember.getRole().getRoleName()
        );

        // 3. Tạo data trả về (cũng nên dùng tiếng Anh cho key)
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUser().getId());
        responseData.put("fullName", updatedMember.getUser().getFullName());
        responseData.put("newRoleCode", updatedMember.getRole().getRoleCode());
        responseData.put("newRoleName", updatedMember.getRole().getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    // API XÓA MỀM THÀNH VIÊN (REMOVED)
    @DeleteMapping("/{companyId}/members/{userId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:remove_member')")
    public ResponseEntity<ApiResponse<Object>> removeMember(
            @PathVariable Integer companyId,
            @PathVariable Integer userId) {

        companyService.removeMemberFromCompany(companyId, userId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Member successfully removed.", null));
    }

    // ========================================================================
    // C. LỜI MỜI (INVITATIONS)
    // ========================================================================

    // API MỜI THÀNH VIÊN VÀO CÔNG TY
    @PostMapping("/{companyId}/invitations")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer companyId,
            @Valid @RequestBody InviteMemberRequest request) {

        companyService.inviteMember(companyId, request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully.", null));
    }

    @GetMapping("/{companyId}/invitations")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:view')") // Hoặc company:edit tùy logic
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyInvitationResponse>>> getCompanyInvitations(
            @PathVariable Integer companyId,
            
            // Thêm các tham số lọc mới
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "PENDING") String status,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        
        PageResponseDTO<CompanyInvitationResponse> response = companyService.getCompanyInvitations(
                companyId, keyword, status, page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Company invitations retrieved successfully.", response));
    }

    // HỦY LỜI MỜI CÔNG TY
    @DeleteMapping("/{companyId}/invitations/{invitationId}")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:edit')") 
    public ResponseEntity<ApiResponse<Object>> cancelCompanyInvitation(
            @PathVariable Integer companyId,
            @PathVariable Integer invitationId
    ) {
        companyService.cancelCompanyInvitation(companyId, invitationId);
        return ResponseEntity.ok(ApiResponse.success("Company invitation cancelled successfully.", null));
    }
}