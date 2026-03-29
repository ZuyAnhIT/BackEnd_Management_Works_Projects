package com.quanlyduan.project_manager_api.controller;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.service.UserService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controller xử lý các nghiệp vụ liên quan đến quản lý hồ sơ cá nhân và bảo mật tài khoản người dùng.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_GET_PROFILE_SUCCESS = "User profile retrieved successfully.";
    private static final String MSG_CHANGE_PASSWORD_SUCCESS = "Your password has been successfully changed.";
    private static final String MSG_UPDATE_PROFILE_SUCCESS = "Profile updated successfully.";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    /**
     * Lấy thông tin hồ sơ chi tiết của người dùng đang đăng nhập.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_PROFILE_SUCCESS, userProfile));
    }

    /**
     * Thực hiện thay đổi mật khẩu đăng nhập cho người dùng hiện tại.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(MSG_CHANGE_PASSWORD_SUCCESS, null));
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân, hỗ trợ tải lên và thay đổi ảnh đại diện (Avatar).
     */
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Parameter(schema = @Schema(implementation = UpdateProfileRequest.class))
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UpdateProfileRequest request = parseUpdateProfileRequest(dataString);
        UserProfileResponse updatedProfile = userService.updateUserProfile(request, file);

        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_PROFILE_SUCCESS, updatedProfile));
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    /**
     * Chuyển đổi dữ liệu chuỗi JSON nhận được từ Multipart Request sang đối tượng DTO.
     */
    private UpdateProfileRequest parseUpdateProfileRequest(String dataString) {
        try {
            return objectMapper.readValue(dataString, UpdateProfileRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON data format: " + e.getMessage());
        }
    }
}