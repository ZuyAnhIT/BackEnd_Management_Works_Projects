package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;

public interface AuthService {
    void register(RegisterRequest request);
    
    void verifyEmail(VerifyEmailRequest request);
}