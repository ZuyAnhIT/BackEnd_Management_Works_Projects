// File: src/main/java/com/quanlyduan/project_manager_api/service/AuthService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    
    void verifyEmail(VerifyEmailRequest request);

    LoginResponse login(LoginRequest request);

    void logout(LogoutRequest request);

    LoginResponse registerFromInvite(RegisterFromInviteRequest request); 

    // Xac thuc quen mat khau
    void forgotPassword(ForgotPasswordRequest request);

    // Doi mat khau khi quen
    void resetPassword(ResetPasswordRequest request);

    LoginResponse loginWithGoogle(GoogleLoginRequest request);
}