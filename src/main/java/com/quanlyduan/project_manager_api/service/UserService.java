// File: src/main/java/com/quanlyduan/project_manager_api/service/UserService.java
package com.quanlyduan.project_manager_api.service;


import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;

public interface UserService {
    void changePassword(ChangePasswordRequest request);

    // Lay thong tin profile
    UserProfileResponse getCurrentUserProfile();

    // Cap nhat thong tin ca nhan
    UserProfileResponse updateUserProfile(UpdateProfileRequest request);
}