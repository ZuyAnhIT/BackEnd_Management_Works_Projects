// File: src/main/java/com/quanlyduan/project_manager_api/controller/CompanyController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor; // Đã xóa

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin("*")

public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // API TAO CONG TY
    @PostMapping
    @PreAuthorize("@securityService.hasSystemPermission('company:create')") 
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        Company newCompany = companyService.createCompany(request); 

        return ResponseEntity
                .status(HttpStatus.CREATED) // Dùng 201 Created cho việc tạo mới
                .body(ApiResponse.success("Tạo công ty thành công.", newCompany)); 
    }

    // (Thêm các API khác cho Company tại đây: GET, PUT, DELETE, ...)

    // API HIEN THI DANH SACH THANH VIEN CONG TY
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')") 
    @GetMapping("/{companyId}/members")
    public ResponseEntity<ApiResponse<List<CompanyMemberResponse>>> getCompanyMembers(
            @PathVariable Integer companyId) {

        List<CompanyMemberResponse> members = companyService.getCompanyMembers(companyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên công ty thành công.", members)); // Đã dịch
    }

    // API HIEN THI THONG TIN CHI TIET CONG TY
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')") // Đã sửa
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompanyDetails(
            @PathVariable Integer companyId) {

        CompanyDetailsResponse companyDetails = companyService.getCompanyDetails(companyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết công ty thành công.", companyDetails)); // Đã dịch
    }

    // API CAP NHAT THONG TIN CONG TY
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:edit')") // Đã sửa
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompany(
            @PathVariable Integer companyId,
            @Valid @RequestBody UpdateCompanyRequest request) {

        CompanyDetailsResponse updatedCompany = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin công ty thành công.", updatedCompany)); // Đã dịch
    }

    // API MOI THANH VIEN VAO CONG TY
    @PostMapping("/{companyId}/invitations")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:invite_member')") // Đã sửa
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer companyId,
            @Valid @RequestBody InviteMemberRequest request) {

        companyService.inviteMember(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Gửi lời mời thành công.", null)); // Đã dịch
    }

    /**
     * API Cập nhật vai trò (Role) của thành viên trong công ty
     */
    @PutMapping("/{companyId}/members/{memberId}/role")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')") // Dùng quyền quản lý
    public ResponseEntity<ApiResponse<Object>> updateCompanyMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        // 1. Gọi service
        CompanyMember updatedMember = companyService.updateCompanyMemberRole(companyId, memberId, request.getRoleCode());

        // 2. Tạo message động
        String message = String.format("Cập nhật vai trò cho người dùng '%s' (ID: %d) thành '%s' thành công.",
            updatedMember.getUser().getFullName(),
            updatedMember.getUser().getId(),
            updatedMember.getRole().getRoleName() // Dùng RoleName cho dễ đọc
        );

        // 3. Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUser().getId());
        responseData.put("fullName", updatedMember.getUser().getFullName());
        responseData.put("newRoleCode", updatedMember.getRole().getRoleCode());
        responseData.put("newRoleName", updatedMember.getRole().getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    // API XOA MEM THANH VIEN
    @DeleteMapping("/{companyId}/members/{userId}")
    // Bảo vệ: Chỉ Admin công ty mới được xóa
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:remove_member')") // Đã sửa (từ delete -> remove_member)
    public ResponseEntity<ApiResponse<Object>> removeMember(
            @PathVariable Integer companyId,
            @PathVariable Integer userId) {
        
        companyService.removeMemberFromCompany(companyId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Xóa thành viên thành công.", null)); // Đã dịch
    }

    // API XEM CHI TIET THANH VIEN
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')") // Đã sửa
    @GetMapping("/{companyId}/members/{memberId}")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> getCompanyMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId) {
        
        CompanyMemberResponse memberDetails = companyService.getCompanyMemberDetails(companyId, memberId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết thành viên thành công.", memberDetails)); // Đã dịch
    }
    
    // API CAP NHAT TRANG THAI THANH VIEN (ACTIVE/SUSPENDED)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')") // Đã sửa (từ edit -> manage_roles)
    @PutMapping("/{companyId}/members/{memberId}/status")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> updateMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {
        
        CompanyMemberResponse updatedMember = companyService.updateMemberStatus(companyId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành viên thành công.", updatedMember)); // Đã dịch
    }
}