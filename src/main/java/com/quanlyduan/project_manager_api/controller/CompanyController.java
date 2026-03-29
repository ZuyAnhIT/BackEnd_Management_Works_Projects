package com.quanlyduan.project_manager_api.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;
import com.quanlyduan.project_manager_api.service.CompanyService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controller xử lý các nghiệp vụ liên quan đến quản lý Công ty (Company), 
 * Thành viên (CompanyMember) và Lời mời (Invitations).
 */
@RestController
@RequestMapping("/api/companies")
@CrossOrigin("*")
public class CompanyController {

    // Khai báo các hằng số phân trang và sắp xếp mặc định
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String SORT_BY_JOINED_AT = "joinedAt";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_DIR_DESC = "desc";
    private static final String STATUS_PENDING = "PENDING";

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_BILLING_INFO_SUCCESS = "Fetched your billing info successfully.";
    private static final String MSG_CREATE_COMPANY_SUCCESS = "Company and default subscription plan created successfully.";
    private static final String MSG_GET_COMPANY_SUCCESS = "Company details retrieved successfully.";
    private static final String MSG_UPDATE_COMPANY_SUCCESS = "Company information updated successfully.";
    private static final String MSG_GET_MEMBERS_SUCCESS = "Company member list retrieved successfully.";
    private static final String MSG_SEARCH_MEMBERS_SUCCESS = "Company member search successful.";
    private static final String MSG_GET_MEMBER_DETAILS_SUCCESS = "Company member details retrieved successfully.";
    private static final String MSG_UPDATE_MEMBER_STATUS_SUCCESS = "Member status updated successfully.";
    private static final String MSG_REMOVE_MEMBER_SUCCESS = "Member successfully removed.";
    private static final String MSG_INVITE_MEMBER_SUCCESS = "Invitation sent successfully.";
    private static final String MSG_GET_INVITATIONS_SUCCESS = "Company invitations retrieved successfully.";
    private static final String MSG_CANCEL_INVITATION_SUCCESS = "Company invitation cancelled successfully.";

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private final CompanyAdminService companyAdminService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc
    public CompanyController(CompanyService companyService, ObjectMapper objectMapper, CompanyAdminService companyAdminService) {
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.companyAdminService = companyAdminService;
    }

    // ========================================================================
    // QUẢN LÝ CÔNG TY (CRUD)
    // ========================================================================

    /**
     * Lấy thông tin thanh toán và hóa đơn của công ty.
     */
    @GetMapping("/{companyId}/billing-info")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:view')")
    public ResponseEntity<ApiResponse<Tenant360Response>> getMyBillingInfo(
            @PathVariable Integer companyId) {
            
        Tenant360Response response = companyAdminService.getTenant360View(companyId);
        return ResponseEntity.ok(ApiResponse.success(MSG_BILLING_INFO_SUCCESS, response));
    }

    /**
     * Tạo công ty mới và tự động cấp phát gói cước SaaS mặc định.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Company>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        Company newCompany = companyService.createCompany(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_COMPANY_SUCCESS, newCompany));
    }

    /**
     * Lấy thông tin chi tiết của công ty.
     */
    @GetMapping("/{companyId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompanyDetails(
            @PathVariable Integer companyId) {

        CompanyDetailsResponse companyDetails = companyService.getCompanyDetails(companyId);
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_COMPANY_SUCCESS, companyDetails));
    }

    /**
     * Cập nhật thông tin công ty (Hỗ trợ tải lên ảnh logo).
     */
    @PutMapping(value = "/{companyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:edit')")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompany(
            @PathVariable Integer companyId,
            @Parameter(schema = @Schema(implementation = UpdateCompanyRequest.class)) 
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UpdateCompanyRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateCompanyRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        CompanyDetailsResponse updatedCompany = companyService.updateCompany(companyId, request, file);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_COMPANY_SUCCESS, updatedCompany));
    }

    // ========================================================================
    // QUẢN LÝ THÀNH VIÊN CÔNG TY
    // ========================================================================

    /**
     * Lấy danh sách thành viên trong công ty.
     */
    @GetMapping("/{companyId}/members")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyMemberResponse>>> getCompanyMembers(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {
            
        PageResponseDTO<CompanyMemberResponse> members = companyService.getCompanyMembers(companyId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_MEMBERS_SUCCESS, members));
    }

    /**
     * Tìm kiếm thành viên công ty dựa trên nhiều tiêu chí (tên, email, chức danh...).
     */
    @GetMapping("/{companyId}/members/search")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyMemberResponse>>> searchCompanyMembers(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {
            
        PageResponseDTO<CompanyMemberResponse> result = companyService.searchCompanyMembers(
            companyId, name, email, jobTitle, roleName, status, phone, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success(MSG_SEARCH_MEMBERS_SUCCESS, result));
    }

    /**
     * Lấy thông tin chi tiết của một thành viên cụ thể.
     */
    @GetMapping("/{companyId}/members/{memberId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:view')")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> getCompanyMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId) {

        CompanyMemberResponse memberDetails = companyService.getCompanyMemberDetails(companyId, memberId);
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_MEMBER_DETAILS_SUCCESS, memberDetails));
    }

    /**
     * Thay đổi trạng thái hoạt động của thành viên (Đình chỉ hoặc kích hoạt lại).
     */
    @PutMapping("/{companyId}/members/{memberId}/status")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')")
    public ResponseEntity<ApiResponse<CompanyMemberResponse>> updateMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {

        CompanyMemberResponse updatedMember = companyService.updateMemberStatus(companyId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_MEMBER_STATUS_SUCCESS, updatedMember));
    }

    /**
     * Cập nhật vai trò (Role) của thành viên trong công ty.
     */
    @PutMapping("/{companyId}/members/{memberId}/role")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:manage_roles')")
    public ResponseEntity<ApiResponse<Object>> updateCompanyMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        CompanyMember updatedMember = companyService.updateCompanyMemberRole(companyId, memberId, request.getRoleCode());

        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
            updatedMember.getUser().getFullName(),
            updatedMember.getUser().getId(),
            updatedMember.getRole().getRoleName()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUser().getId());
        responseData.put("fullName", updatedMember.getUser().getFullName());
        responseData.put("newRoleCode", updatedMember.getRole().getRoleCode());
        responseData.put("newRoleName", updatedMember.getRole().getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    /**
     * Xóa mềm thành viên khỏi công ty (Chuyển trạng thái sang REMOVED).
     */
    @DeleteMapping("/{companyId}/members/{userId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:remove_member')")
    public ResponseEntity<ApiResponse<Object>> removeMember(
            @PathVariable Integer companyId,
            @PathVariable Integer userId) {

        companyService.removeMemberFromCompany(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(MSG_REMOVE_MEMBER_SUCCESS, null));
    }

    // ========================================================================
    // QUẢN LÝ LỜI MỜI (INVITATIONS)
    // ========================================================================

    /**
     * Gửi lời mời tham gia công ty qua email.
     */
    @PostMapping("/{companyId}/invitations")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'company:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer companyId,
            @Valid @RequestBody InviteMemberRequest request) {

        companyService.inviteMember(companyId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_INVITE_MEMBER_SUCCESS, null));
    }

    /**
     * Lấy danh sách các lời mời đã gửi (Có thể lọc theo trạng thái, từ khóa).
     */
    @GetMapping("/{companyId}/invitations")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<CompanyInvitationResponse>>> getCompanyInvitations(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = STATUS_PENDING) String status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {
        
        PageResponseDTO<CompanyInvitationResponse> response = companyService.getCompanyInvitations(
                companyId, keyword, status, page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_INVITATIONS_SUCCESS, response));
    }

    /**
     * Hủy một lời mời tham gia công ty đã gửi trước đó.
     */
    @DeleteMapping("/{companyId}/invitations/{invitationId}")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:edit')") 
    public ResponseEntity<ApiResponse<Object>> cancelCompanyInvitation(
            @PathVariable Integer companyId,
            @PathVariable Integer invitationId) {
            
        companyService.cancelCompanyInvitation(companyId, invitationId);
        return ResponseEntity.ok(ApiResponse.success(MSG_CANCEL_INVITATION_SUCCESS, null));
    }
}