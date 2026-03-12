// File: src/main/java/com/quanlyduan/project_manager_api/service/CompanyService.java
package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;


/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Công ty (Company).
 * Bao gồm tạo công ty, quản lý thành viên, và luồng mời.
 */
public interface CompanyService {
    
    // ========================================================================
    // 1. QUẢN LÝ CÔNG TY (CRUD & LIFECYCLE)
    // ========================================================================

    /**
     * Tạo một Công ty mới.
     */
    Company createCompany(CreateCompanyRequest request); 

    /**
     * Lấy thông tin chi tiết của Công ty.
     * @param companyId ID công ty
     * @return CompanyDetailsResponse DTO
     */
    CompanyDetailsResponse getCompanyDetails(Integer companyId);

    /**
     * Cập nhật thông tin công ty và logo (sử dụng MultipartFile).
     */
    CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request, MultipartFile logoFile);
    
    
    // ========================================================================
    // 2. QUẢN LÝ THÀNH VIÊN (MEMBERSHIP & ROLES)
    // ========================================================================

    /**
     * Mời một thành viên mới vào Công ty (Gửi Email với Token).
     * @param companyId ID công ty
     * @param request DTO chứa email và roleCode
     */
    CompanyInvitation inviteMember(Integer companyId, InviteMemberRequest request); 

    /**
     * Chấp nhận lời mời tham gia Công ty (sau khi click link).
     */
    CompanyDetailsResponse acceptInvitation(AcceptInvitationRequest request);

    /**
     * Lấy thông tin chi tiết của một thành viên trong công ty.
     * @param companyId ID của công ty (để kiểm tra bảo mật)
     * @param memberId ID của bản ghi CompanyMember
     */
    CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId);
    
    /**
     * Cập nhật trạng thái của thành viên (ACTIVE/SUSPENDED/RESTORE).
     * @param request DTO chứa trạng thái mới
     */
    CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request);

    /**
     * Cập nhật vai trò (Role) của một thành viên trong công ty.
     * @param newRoleCode Mã vai trò mới (ví dụ: "COMPANY_MEMBER")
     * @return CompanyMember Entity đã cập nhật
     */
    CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode);

    /**
     * Xóa mềm (Soft Delete) một thành viên khỏi công ty (chuyển status thành REMOVED).
     * @param userId ID người dùng bị xóa
     */
    void removeMemberFromCompany(Integer companyId, Integer userId);

    // ========================================================================
    // 3. XEM DANH SÁCH (LISTING & FILTERING)
    // ========================================================================

    /**
     * Lấy danh sách thành viên công ty (Phân trang & Sắp xếp).
     * @param sortBy Trường cần sắp xếp
     * @param sortDir Hướng sắp xếp
     */
    PageResponseDTO<CompanyMemberResponse> getCompanyMembers(Integer companyId, int page, int size, String sortBy, String sortDir);

    /**
     * Tìm kiếm thành viên công ty (Nâng cao: Tên, Email, Chức vụ, Role, SĐT).
     */
    PageResponseDTO<CompanyMemberResponse> searchCompanyMembers(
            Integer companyId, 
            String searchName, 
            String searchEmail, 
            String searchJobTitle, 
            String searchRoleName,
            MemberStatus searchStatus,
            String searchPhone, 
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Lấy danh sách lời mời công ty có lọc và phân trang.
     */
    PageResponseDTO<CompanyInvitationResponse> getCompanyInvitations(
            Integer companyId, 
            String keyword, // Tìm theo email
            String status,  // Lọc theo trạng thái
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Lấy chi tiết lời mời (public) để frontend quyết định luồng (Login/Register).
     */
    InvitationDetailsResponse getInvitationDetails(String token);
    
    /**
     * Hủy lời mời tham gia công ty.
     * @param companyId ID công ty
     * @param invitationId ID lời mời cần hủy
     */
    void cancelCompanyInvitation(Integer companyId, Integer invitationId);



}