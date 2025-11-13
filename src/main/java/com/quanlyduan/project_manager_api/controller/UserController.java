// File: src/main/java/com/quanlyduan/project_manager_api/controller/UserController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    // API CAP NHAT THONG TIN CA NHAN
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        
        UserProfileResponse updatedProfile = userService.updateUserProfile(request);
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công.", updatedProfile)); // Đã dịch
    }
}