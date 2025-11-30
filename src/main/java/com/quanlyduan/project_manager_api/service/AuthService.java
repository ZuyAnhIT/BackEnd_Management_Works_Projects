// File: src/main/java/com.quanlyduan.project_manager_api/service/AuthService.java
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
 * Interface Service quản lý các nghiệp vụ liên quan đến Xác thực (Authentication) và Ủy quyền (Authorization) cơ bản.
 * Bao gồm đăng ký, đăng nhập, và quản lý mật khẩu.
 */
public interface AuthService {
    
    // ========================================================================
    // 1. ĐĂNG KÝ & XÁC THỰC (REGISTRATION & VERIFICATION)
    // ========================================================================

    /**
     * Đăng ký tài khoản người dùng mới (Standard Registration).
     * @param request DTO chứa thông tin đăng ký.
     */
    void register(RegisterRequest request);
    
    /**
     * Xác thực Email bằng mã OTP.
     * @param request DTO chứa email và mã OTP.
     */
    void verifyEmail(VerifyEmailRequest request);

    // ========================================================================
    // 2. ĐĂNG NHẬP & ĐĂNG XUẤT (LOGIN & LOGOUT)
    // ========================================================================

    /**
     * Đăng nhập bằng Email và Mật khẩu.
     * @param request DTO chứa email và mật khẩu.
     * @return LoginResponse chứa Access Token và Refresh Token.
     */
    LoginResponse login(LoginRequest request);

    /**
     * Đăng nhập bằng tài khoản Google (OAuth2/OIDC).
     * @param request DTO chứa Google ID Token.
     * @return LoginResponse chứa Tokens.
     */
    LoginResponse loginWithGoogle(GoogleLoginRequest request);

    /**
     * Đăng xuất.
     * @param request DTO chứa Refresh Token cần vô hiệu hóa.
     */
    void logout(LogoutRequest request);

    // ========================================================================
    // 3. QUẢN LÝ MẬT KHẨU (PASSWORD MANAGEMENT)
    // ========================================================================

    /**
     * Gửi liên kết hoặc mã đặt lại mật khẩu đến email.
     * @param request DTO chứa email người dùng.
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Đặt lại mật khẩu mới bằng token xác thực.
     * @param request DTO chứa token và mật khẩu mới.
     */
    void resetPassword(ResetPasswordRequest request);

    // ========================================================================
    // 4. ĐĂNG KÝ TỪ LỜI MỜI (INVITATION FLOWS)
    // ========================================================================

    /**
     * Đăng ký tài khoản mới từ lời mời tham gia CÔNG TY.
     * @param request DTO chứa thông tin đăng ký và token lời mời.
     * @return LoginResponse chứa Tokens.
     */
    LoginResponse registerFromInvite(RegisterFromInviteRequest request); 

    /**
     * Đăng ký tài khoản mới từ lời mời tham gia DỰ ÁN (Khách/Người ngoài).
     * @param request DTO chứa thông tin đăng ký và token lời mời dự án.
     * @return LoginResponse chứa Tokens.
     */
    LoginResponse registerFromProjectInvite(RegisterFromProjectInviteRequest request);
}