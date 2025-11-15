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
            "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP.", 
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
            "Đăng nhập thành công.",
            loginResponse
        );
        return ResponseEntity.ok(response);
    }

    // API DANG XUAT
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công.", null));
    }


    // API XAC THUC 
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyRequest) {
        authService.verifyEmail(verifyRequest);
        
        
        ApiResponse<Object> response = ApiResponse.success(
            "Xác thực email thành công.", 
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
            "Đăng ký và tham gia công ty thành công.", loginResponse
        ));
    }

    // API QUEN MAT KHAU
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        authService.forgotPassword(request);
        
        // Luôn trả về thành công để bảo mật (tránh dò email)
        return ResponseEntity.ok(ApiResponse.success(
            "Nếu tài khoản với email này tồn tại, liên kết đặt lại mật khẩu đã được gửi.", // Đã dịch
            null
        ));
    }

    // API DAT LAI MAT KHAU
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        authService.resetPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Mật khẩu của bạn đã được đặt lại thành công. Bây giờ bạn có thể đăng nhập ngay bây giờ.", // Đã dịch
            null
        ));
    }

    // API DANG NHAP BANG GOOGLE
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {
        
        LoginResponse loginResponse = authService.loginWithGoogle(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            " Đăng nhập bằng Google thành công", 
            loginResponse
        ));
    }
    
}