package com.quanlyduan.project_manager_api.controller;


import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        
        
        ApiResponse<Object> response = ApiResponse.success(
            "Đăng ký thành công. Vui lòng kiểm tra email để xác thực (OTP).", 
            null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest verifyRequest) {
        authService.verifyEmail(verifyRequest);
        
        
        ApiResponse<Object> response = ApiResponse.success(
            "Xác thực email thành công.", 
            null
        );
        return ResponseEntity.ok(response);
    }
}