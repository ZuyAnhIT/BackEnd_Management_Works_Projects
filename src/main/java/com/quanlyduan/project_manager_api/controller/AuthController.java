// File: src/main/java/com/quanlyduan/project_manager_api/controller/AuthController.java
package com.quanlyduan.project_manager_api.controller;


import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // API ĐANG KY
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        
        
        ApiResponse<Object> response = ApiResponse.success(
            "Registration successful. Please check your email for OTP verification.", 
            null
        );
        return ResponseEntity.ok(response);
    }
    
    // API DANG NHAP
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        // Gọi service để đăng nhập
        LoginResponse loginResponse = authService.login(loginRequest);
        
        // Trả về ApiResponse chứa Access Token và Refresh Token
        ApiResponse<LoginResponse> response = ApiResponse.success(
            "Login successful",
            loginResponse
        );
        return ResponseEntity.ok(response);
    }

    // API DANG XUAT
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }


    // API XAC THUC 
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyRequest) {
        authService.verifyEmail(verifyRequest);
        
        
        ApiResponse<Object> response = ApiResponse.success(
            "Email verified successfully.", 
            null
        );
        return ResponseEntity.ok(response);
    }


    // API DANG KY KHI THAM GIA THEO LOI MOI
    // Đây là API public, không cần xác thực
    @PostMapping("/register-from-invite")
    public ResponseEntity<ApiResponse<LoginResponse>> registerFromInvite(
            @Valid @RequestBody RegisterFromInviteRequest request) {
        
        LoginResponse loginResponse = authService.registerFromInvite(request);
        return ResponseEntity.ok(ApiResponse.success(
            "Registration and company join successful", loginResponse
        ));
    }

    // API QUEN MAT KHAU
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        authService.forgotPassword(request);
        
        // Luôn trả về thành công để bảo mật (tránh dò email)
        return ResponseEntity.ok(ApiResponse.success(
            "If an account with this email exists, a password reset link has been sent.", // Đã dịch
            null
        ));
    }

    // API DAT LAI MAT KHAU
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        authService.resetPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Password has been reset successfully. You can now log in.", // Đã dịch
            null
        ));
    }

    // API DANG NHAP BANG GOOGLE
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {
        
        LoginResponse loginResponse = authService.loginWithGoogle(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Google login successful", 
            loginResponse
        ));
    }
    
}