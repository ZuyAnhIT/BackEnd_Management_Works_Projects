// File: src/main/java/com/quanlyduan/project_manager_api/service/CompanyService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.model.Company; // Đã dịch
import com.quanlyduan.project_manager_api.model.CompanyMember;

public interface CompanyService {
    Company createCompany(CreateCompanyRequest request); // Đã dịch

    void inviteMember(Integer companyId, InviteMemberRequest request); // Đã dịch

    void acceptInvitation(AcceptInvitationRequest request);

    // Danh sach thanh vien cong ty 
    List<CompanyMemberResponse> getCompanyMembers(Integer companyId); // Đã dịch

    // Xem chi tiet cong ty
    CompanyDetailsResponse getCompanyDetails(Integer companyId); // Đã dịch

    // Update thong tin công ty
    CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request); // Đã dịch
    
    /**
     * Xóa mềm một thành viên khỏi công ty (chuyển status thành REMOVED).
     * @param companyId ID công ty
     * @param userId ID người dùng bị xóa
     */
    void removeMemberFromCompany(Integer companyId, Integer userId);

    /**
     * Lấy thông tin chi tiết của một thành viên trong công ty.
     * @param companyId ID của công ty (để kiểm tra bảo mật)
     * @param memberId ID của bản ghi CompanyMember
     * @return CompanyMemberResponse DTO
     */
    CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId);

    /**
     * Cập nhật trạng thái của thành viên (ACTIVE/SUSPENDED).
     * @param companyId ID công ty
     * @param memberId ID của bản ghi CompanyMember
     * @param request DTO chứa trạng thái mới
     * @return CompanyMemberResponse DTO đã cập nhật
     */
    CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request);

/**
     * Lấy chi tiết lời mời (public) để frontend quyết định luồng.
     * @param token Token từ link
     * @return DTO chứa email, tên cty, và user đã tồn tại hay chưa
     */
    InvitationDetailsResponse getInvitationDetails(String token);
    
    /**
     * Cập nhật vai trò (Role) của một thành viên trong công ty.
     * @param companyId ID công ty
     * @param memberId ID của bản ghi CompanyMember
     * @param newRoleCode Mã vai trò mới (ví dụ: "COMPANY_MEMBER")
     * @return CompanyMember Entity đã cập nhật (để Controller lấy thông tin)
     */
    CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode);
}
