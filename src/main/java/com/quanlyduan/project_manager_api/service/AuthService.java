package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromProjectInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;

/**
 * Interface Service quan ly toan bo nghiep vu Xac thuc va Uy quyen.
 * Xu ly cac luong dang ky thong thuong, dang ky qua loi moi, dang nhap da phuong thuc va bao mat tai khoan.
 */
public interface AuthService {

    // ======================================================
    // 1. DANG KY VA XAC THUC (REGISTRATION & VERIFICATION)
    // ======================================================

    /**
     * Dang ky tai khoan nguoi dung moi theo quy trinh tieu chuan.
     * * @param request Du lieu dang ky tai khoan
     */
    void register(RegisterRequest request);
    
    /**
     * Kich hoat tai khoan bang cach xac thuc Email qua ma OTP.
     * * @param request Thong tin email va ma xac thuc
     */
    void verifyEmail(VerifyEmailRequest request);

    // ======================================================
    // 2. DANG NHAP VA DANG XUAT (LOGIN & LOGOUT)
    // ======================================================

    /**
     * Dang nhap he thong bang Email va Mat khau.
     * * @param request Thong tin dang nhap
     * @return Phan hoi chua Access Token va Refresh Token
     */
    LoginResponse login(LoginRequest request);

    /**
     * Dang nhap nhanh su dung tai khoan Google (OAuth2).
     * * @param request Google ID Token duoc cung cap tu Frontend
     * @return Phan hoi chua thong tin phien dang nhap moi
     */
    LoginResponse loginWithGoogle(GoogleLoginRequest request);

    /**
     * Dang xuat va vo hieu hoa phien lam viec hien tai.
     * * @param request Thong tin Token can huy bo
     */
    void logout(LogoutRequest request);

    // ======================================================
    // 3. QUAN LY MAT KHAU (PASSWORD MANAGEMENT)
    // ======================================================

    /**
     * Yeu cau cap lai mat khau qua Email xac nhan.
     * * @param request Email cua tai khoan can khoi phuc
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Thiet lap mat khau moi sau khi da xac thuc yeu cau khoi phuc.
     * * @param request Token xac thuc va mat khau moi
     */
    void resetPassword(ResetPasswordRequest request);

    // ======================================================
    // 4. LUONG DANG KY QUA LOI MOI (INVITATION FLOWS)
    // ======================================================

    /**
     * Dang ky tai khoan moi va tu dong gia nhap CONG TY theo loi moi.
     * * @param request Thong tin dang ky kem theo Invitation Token cua Cong ty
     * @return Phan hoi dang nhap sau khi dang ky thanh cong
     */
    LoginResponse registerFromInvite(RegisterFromInviteRequest request); 

    /**
     * Dang ky tai khoan moi va gia nhap truc tiep vao DU AN (danh cho Khach/doi tac).
     * * @param request Thong tin dang ky kem theo Invitation Token cua Du an
     * @return Phan hoi dang nhap sau khi dang ky thanh cong
     */
    LoginResponse registerFromProjectInvite(RegisterFromProjectInviteRequest request);
}