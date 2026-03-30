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
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

/**
 * Service quan ly toan bo vong doi cua Cong ty (Company) va thanh vien (Membership).
 * Xu ly tu khau khoi tao doanh nghiep, quan tri nhan su den quy trinh moi tham gia he thong.
 */
public interface CompanyService {

    // ======================================================
    // 1. QUAN LY DOANH NGHIEP (COMPANY LIFECYCLE)
    // ======================================================

    /**
     * Khoi tao mot Cong ty moi tren he thong Worknet.
     * * @param request Thong tin co ban cua cong ty
     * @return Entity Cong ty sau khi luu tru
     */
    Company createCompany(CreateCompanyRequest request); 

    /**
     * Truy xuat thong tin chi tiet va trang thai hien tai cua Cong ty.
     * * @param companyId ID dinh danh cong ty
     * @return DTO chua thong tin chi tiet cong ty
     */
    CompanyDetailsResponse getCompanyDetails(Integer companyId);

    /**
     * Cap nhat thong tin ho so va thay doi Logo doanh nghiep.
     * * @param companyId ID dinh danh cong ty
     * @param request Cac truong thong tin can thay doi
     * @param logoFile Tep tin hinh anh Logo moi
     * @return Thong tin cong ty sau khi cap nhat
     */
    CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request, MultipartFile logoFile);
    
    // ======================================================
    // 2. QUAN LY THANH VIEN (MEMBERSHIP & ROLES)
    // ======================================================

    /**
     * Gui loi moi gia nhap cong ty den mot Email cu the.
     * * @param companyId ID cong ty phat hanh loi moi
     * @param request Email nguoi nhan va vai tro du kien
     * @return Entity Loi moi voi Token xac thuc duy nhat
     */
    CompanyInvitation inviteMember(Integer companyId, InviteMemberRequest request); 

    /**
     * Xac nhan loi moi va tro thanh thanh vien chinh thuc cua cong ty.
     * * @param request Token xac thuc va thong tin bo sung tu nguoi dung
     * @return Thong tin chi tiet cong ty sau khi gia nhap thanh cong
     */
    CompanyDetailsResponse acceptInvitation(AcceptInvitationRequest request);

    /**
     * Truy xuat ho so chi tiet cua mot thanh vien ben trong to chuc.
     * * @param companyId ID cong ty (dung de kiem soat truy cap)
     * @param memberId ID ban ghi thanh vien
     * @return DTO ho so thanh vien
     */
    CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId);
    
    /**
     * Thay doi trang thai lam viec cua thanh vien (vi du: Tam khoa hoac Khoi phuc).
     * * @param companyId ID cong ty
     * @param memberId ID ban ghi thanh vien
     * @param request Trang thai moi can thiet lap
     * @return Thong tin thanh vien sau khi cap nhat trang thai
     */
    CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request);

    /**
     * Thay doi vai tro phan quyen cho thanh vien ben trong to chuc.
     * * @param companyId ID cong ty
     * @param memberId ID ban ghi thanh vien
     * @param newRoleCode Ma vai tro moi (vi du: COMPANY_ADMIN)
     * @return Entity thanh vien sau khi gan vai tro moi
     */
    CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode);

    /**
     * Ngung tu cach thanh vien cua nguoi dung khoi cong ty (Soft Delete).
     * * @param companyId ID cong ty
     * @param userId ID nguoi dung can loai bo
     */
    void removeMemberFromCompany(Integer companyId, Integer userId);

    // ======================================================
    // 3. TRA CUU VA LOC DU LIEU (LISTING & SEARCH)
    // ======================================================

    /**
     * Lay danh sach thanh vien thuoc to chuc voi tinh nang phan trang.
     * * @param companyId ID cong ty
     * @return Trang danh sach thanh vien
     */
    PageResponseDTO<CompanyMemberResponse> getCompanyMembers(Integer companyId, int page, int size, String sortBy, String sortDir);

    /**
     * Tim kiem thanh vien nang cao dua tren nhieu tieu chi loc.
     * * @return Ket qua tim kiem phan trang phu hop bo loc
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
     * Quan ly danh sach cac loi moi da gui tu cong ty.
     * * @param companyId ID cong ty
     * @return Trang danh sach cac loi moi
     */
    PageResponseDTO<CompanyInvitationResponse> getCompanyInvitations(
            Integer companyId, 
            String keyword, 
            String status,  
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Lay thong tin loi moi tu Token (Dung cho luong Public truoc khi dang nhap).
     * * @param token Ma xac thuc loi moi duy nhat
     * @return Chi tiet loi moi phuc vu luong dang ky/dang nhap
     */
    InvitationDetailsResponse getInvitationDetails(String token);
    
    /**
     * Huy bo mot loi moi da gui nhung chua duoc chap nhan.
     * * @param companyId ID cong ty
     * @param invitationId ID ban ghi loi moi can huy
     */
    void cancelCompanyInvitation(Integer companyId, Integer invitationId);
}