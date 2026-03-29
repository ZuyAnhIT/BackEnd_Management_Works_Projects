package com.quanlyduan.project_manager_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromProjectInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.service.AuthService;

/**
 * Controller xử lý các nghiệp vụ liên quan đến xác thực người dùng (Authentication).
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    // Khai báo các câu thông báo trả về
    private static final String MSG_REGISTER_SUCCESS = "Registration successful. Please check your email for OTP verification.";
    private static final String MSG_LOGIN_SUCCESS = "Login successful.";
    private static final String MSG_LOGOUT_SUCCESS = "Logout successful.";
    private static final String MSG_VERIFY_SUCCESS = "Email verification successful.";
    private static final String MSG_REGISTER_INVITE_SUCCESS = "Registration and company acceptance successful.";
    private static final String MSG_REGISTER_PROJECT_INVITE_SUCCESS = "Registration and project acceptance successful.";
    private static final String MSG_FORGOT_PASSWORD_SUCCESS = "If an account with this email exists, a password reset link has been sent.";
    private static final String MSG_RESET_PASSWORD_SUCCESS = "Your password has been successfully reset. You can log in now.";
    private static final String MSG_GOOGLE_LOGIN_SUCCESS = "Google login successful.";

    private final AuthService authService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Đăng ký tài khoản mới và gửi mã OTP qua email.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_REGISTER_SUCCESS, null));
    }

    /**
     * Đăng nhập hệ thống bằng email và mật khẩu.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_LOGIN_SUCCESS, response));
    }

    /**
     * Đăng xuất và thu hồi Refresh Token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_LOGOUT_SUCCESS, null));
    }

    /**
     * Xác thực email tài khoản thông qua mã OTP.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_VERIFY_SUCCESS, null));
    }

    /**
     * Đăng ký tài khoản mới thông qua lời mời tham gia công ty.
     * Áp dụng cho người dùng chưa có tài khoản trên hệ thống.
     */
    @PostMapping("/register-from-invite")
    public ResponseEntity<ApiResponse<LoginResponse>> registerFromInvite(
            @Valid @RequestBody RegisterFromInviteRequest request) {
        LoginResponse response = authService.registerFromInvite(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_REGISTER_INVITE_SUCCESS, response));
    }

    /**
     * Đăng ký tài khoản mới thông qua lời mời tham gia dự án.
     * Tự động thiết lập quyền dự án và đăng nhập.
     */
    @PostMapping("/register-from-project-invite")
    public ResponseEntity<ApiResponse<LoginResponse>> registerFromProjectInvite(
            @Valid @RequestBody RegisterFromProjectInviteRequest request) {
        LoginResponse response = authService.registerFromProjectInvite(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_REGISTER_PROJECT_INVITE_SUCCESS, response));
    }

    /**
     * Gửi yêu cầu cấp lại mật khẩu.
     * Luôn trả về thông báo thành công để bảo mật, tránh việc kẻ xấu dò tìm email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_FORGOT_PASSWORD_SUCCESS, null));
    }

    /**
     * Đặt lại mật khẩu mới thông qua liên kết bảo mật.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_RESET_PASSWORD_SUCCESS, null));
    }

    /**
     * Đăng nhập hoặc đăng ký nhanh thông qua tài khoản Google.
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_GOOGLE_LOGIN_SUCCESS, response));
    }
}