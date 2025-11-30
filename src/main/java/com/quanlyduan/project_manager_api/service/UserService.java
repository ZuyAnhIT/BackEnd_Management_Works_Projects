// File: src/main/java/com.quanlyduan/project_manager_api/service/UserService.java
package com.quanlyduan.project_manager_api.service;


import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến thông tin người dùng (User Profile).
 */
public interface UserService {
    
    /**
     * Thay đổi mật khẩu người dùng.
     * @param request DTO chứa mật khẩu cũ và mật khẩu mới.
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Lấy toàn bộ thông tin hồ sơ cá nhân của người dùng đang đăng nhập.
     * @return UserProfileResponse DTO chi tiết.
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Cập nhật thông tin cá nhân và ảnh đại diện (avatar).
     * @param request DTO chứa các trường thông tin cập nhật (Partial Update).
     * @param avatarFile File ảnh mới (MultipartFile) được gửi lên.
     * @return UserProfileResponse DTO sau khi đã cập nhật.
     */
    UserProfileResponse updateUserProfile(UpdateProfileRequest request, MultipartFile avatarFile);
}