// File: src/main/java/com/quanlyduan/project_manager_api/controller/AuthController.java
package com.quanlyduan.project_manager_api.controller;


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
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Xác thực (Authentication).
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ======================================================
    // 1. ĐĂNG KÝ (REGISTER)
    // ======================================================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Gọi service để đăng ký user và gửi OTP
        authService.register(registerRequest);

        // Sửa thông báo trả về sang tiếng Anh
        ApiResponse<Object> response = ApiResponse.success(
            "Registration successful. Please check your email for OTP verification.",
            null
        );
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // 2. ĐĂNG NHẬP (LOGIN)
    // ======================================================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Gọi service để đăng nhập và lấy tokens
        LoginResponse loginResponse = authService.login(loginRequest);

        // Sửa thông báo trả về sang tiếng Anh
        ApiResponse<LoginResponse> response = ApiResponse.success(
            "Login successful.",
            loginResponse
        );
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // 3. ĐĂNG XUẤT (LOGOUT)
    // ======================================================
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        // Thu hồi Refresh Token
        authService.logout(logoutRequest);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Logout successful.", null));
    }


    // ======================================================
    // 4. XÁC THỰC EMAIL (VERIFY EMAIL)
    // ======================================================
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyRequest) {
        // Xác thực OTP
        authService.verifyEmail(verifyRequest);

        // Sửa thông báo trả về sang tiếng Anh
        ApiResponse<Object> response = ApiResponse.success(
            "Email verification successful.",
            null
        );
        return ResponseEntity.ok(response);
    }


    // ======================================================
    // 5. ĐĂNG KÝ TỪ LỜI MỜI CÔNG TY (REGISTER FROM COMPANY INVITE)
    // ======================================================
    // API public, dùng cho người chưa có tài khoản. Trả về token.
    @PostMapping("/register-from-invite")
    public ResponseEntity<ApiResponse<LoginResponse>> registerFromInvite(
            @Valid @RequestBody RegisterFromInviteRequest request) {

        LoginResponse loginResponse = authService.registerFromInvite(request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
            "Registration and company acceptance successful.", loginResponse
        ));
    }

    // ======================================================
    // 6. ĐĂNG KÝ TỪ LỜI MỜI DỰ ÁN (REGISTER FROM PROJECT INVITE)
    // ======================================================
    // API public, dùng cho người chưa có tài khoản. Trả về token.
    @PostMapping("/register-from-project-invite")
    public ResponseEntity<ApiResponse<LoginResponse>> registerFromProjectInvite(
            @Valid @RequestBody RegisterFromProjectInviteRequest request) {

        // Gọi logic để tạo user, gán role project và đăng nhập
        LoginResponse loginResponse = authService.registerFromProjectInvite(request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
            "Registration and project acceptance successful.", loginResponse
        ));
    }

    // ======================================================
    // 7. QUÊN MẬT KHẨU (FORGOT PASSWORD)
    // ======================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        // Luôn trả về thành công để bảo mật (tránh dò email)
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
            "If an account with this email exists, a password reset link has been sent.",
            null
        ));
    }

    // ======================================================
    // 8. ĐẶT LẠI MẬT KHẨU (RESET PASSWORD)
    // ======================================================
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
            "Your password has been successfully reset. You can log in now.",
            null
        ));
    }

    // ======================================================
    // 9. ĐĂNG NHẬP BẰNG GOOGLE (LOGIN WITH GOOGLE)
    // ======================================================
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {

        LoginResponse loginResponse = authService.loginWithGoogle(request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success(
            "Google login successful.",
            loginResponse
        ));
    }

}