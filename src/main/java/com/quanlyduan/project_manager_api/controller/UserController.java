// File: src/main/java/com/quanlyduan/project_manager_api/controller/UserController.java
package com.quanlyduan.project_manager_api.controller;

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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Hồ sơ người dùng (User Profile).
 * Các API này thường yêu cầu xác thực (authenticated user).
 */
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    // ======================================================
    // 1. LẤY THÔNG TIN CÁ NHÂN ĐẦY ĐỦ (GET PROFILE)
    // ======================================================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        // API này tự động được bảo vệ (yêu cầu token) qua cấu hình Security
        UserProfileResponse userProfile = userService.getCurrentUserProfile();
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully.", userProfile));
    }

    // ======================================================
    // 2. ĐỔI MẬT KHẨU (CHANGE PASSWORD)
    // ======================================================
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Your password has been successfully changed.", null));
    }

    // ======================================================
    // 3. CẬP NHẬT HỒ SƠ (UPDATE PROFILE - KÈM UPLOAD AVATAR)
    // ======================================================
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            // Nhận JSON String (data)
            @Parameter(schema = @Schema(implementation = UpdateProfileRequest.class))
            @RequestPart("data") String dataString,

            // Nhận file ảnh (Optional)
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        // 1. Tự tay convert từ String sang DTO
        UpdateProfileRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateProfileRequest.class);
        } catch (JsonProcessingException e) {
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        // 2. Gọi Service (Xử lý file và cập nhật DB)
        UserProfileResponse updatedProfile = userService.updateUserProfile(request, file);

        // 3. Trả về kết quả
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", updatedProfile));
    }
}