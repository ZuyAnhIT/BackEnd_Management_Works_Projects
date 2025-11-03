package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    
    void verifyEmail(VerifyEmailRequest request);

    LoginResponse login(LoginRequest request);
}