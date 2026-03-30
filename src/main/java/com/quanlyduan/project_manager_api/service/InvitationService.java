package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;

/**
 * Service ho tro xu ly logic chung cho cac loai loi moi (Invitation).
 * Cung cap cac phuong thuc xac thuc token va thiet lap quan he thanh vien, 
 * duoc su dung boi ca AuthModule va CompanyModule.
 */
public interface InvitationService {

    // ======================================================
    // 1. XAC THUC VA KIEM TRA (VALIDATION)
    // ======================================================

    /**
     * Kiem tra tinh hop le cua mot ma xac thuc loi moi (Invitation Token).
     * Quy trinh bao gom kiem tra: Ton tai, Thoi han hieu luc, va Trang thai cho (PENDING).
     * * @param token Chuoi ma xac thuc tu duong dan (URL)
     * @return Entity Loi moi neu hop le
     * @throws ResourceNotFoundException Neu token khong ton tai trong he thong
     * @throws BadRequestException Neu token da het han hoac da duoc su dung truoc do
     */
    CompanyInvitation validateInvitationToken(String token);

    // ======================================================
    // 2. HANH DONG GIA NHAP (ONBOARDING ACTIONS)
    // ======================================================

    /**
     * Thiet lap quyen thanh vien cho mot nguoi dung tai mot Cong ty cu the.
     * Ham nay dung de anh xa nguoi dung vao ma tran nhan su cua doanh nghiep (CompanyMember).
     * * @param user Doi tuong nguoi dung (Moi hoac Cu)
     * @param company Cong ty ma nguoi dung se gia nhap
     * @param role Vai tro (Role) se duoc gan cho nguoi dung tai Cong ty do
     */
    void addMemberToCompany(User user, Company company, Role role);
}