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
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    // API DOI MAT KHAU
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu của bạn thành công.", null)); // Đã dịch
    }

    // API LAY DAY DU THONG TIN CA NHAN
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        // API này tự động được bảo vệ (yêu cầu token)
        // vì nó không nằm trong PUBLIC_URLS
        UserProfileResponse userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công.", userProfile)); // Đã dịch
    }
    // (Sau này chúng ta sẽ thêm endpoint GET /api/users/me để lấy thông tin user)

    // API CAP NHAT THONG TIN CA NHAN (TICH HOP UPLOAD)
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            // Thay đổi: Nhận "data" là String thay vì Object
            @Parameter(schema = @Schema(implementation = UpdateProfileRequest.class))
            @RequestPart("data") String dataString, 
            
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        
        // 3. Tự tay convert từ String sang DTO
        UpdateProfileRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateProfileRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Dữ liệu JSON không hợp lệ: " + e.getMessage());
        }

        // 4. (Tùy chọn) Validate thủ công nếu cần, hoặc để Service lo logic
        
        UserProfileResponse updatedProfile = userService.updateUserProfile(request, file);
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công.", updatedProfile));
    }
}