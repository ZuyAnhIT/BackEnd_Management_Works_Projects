package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.model.CongTy;

public interface CompanyService {
    CongTy createCompany(CreateCompanyRequest request);

    void inviteMember(Integer congTyId, InviteMemberRequest request);

    void acceptInvitation(AcceptInvitationRequest request);

    // Danh sach thanh vien cong ty 
    List<CompanyMemberResponse> getCompanyMembers(Integer congTyId);

    // Xem chi tiet cong ty
    CompanyDetailsResponse getCompanyDetails(Integer congTyId);

    // Update thong tin công ty
    CompanyDetailsResponse updateCompany(Integer congTyId, UpdateCompanyRequest request);
    
}