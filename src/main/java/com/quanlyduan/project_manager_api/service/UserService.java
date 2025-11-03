package com.quanlyduan.project_manager_api.service;


import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;

public interface UserService {
    void changePassword(ChangePasswordRequest request);
}
