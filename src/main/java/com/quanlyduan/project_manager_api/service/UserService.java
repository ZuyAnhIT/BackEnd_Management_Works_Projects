package com.quanlyduan.project_manager_api.service;


import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;

public interface UserService {
    void changePassword(ChangePasswordRequest request);

    UserProfileResponse getCurrentUserProfile();
}
