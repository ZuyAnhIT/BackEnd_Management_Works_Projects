// File: src/main/java/com/quanlyduan/project_manager_api/service/CompanyService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
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
     * User Story 1: Phân quyền thành viên công ty
     */
    CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode);

    
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
}
