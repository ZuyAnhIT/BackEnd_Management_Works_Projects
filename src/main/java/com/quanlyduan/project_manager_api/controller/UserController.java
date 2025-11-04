package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    // API DOI MAT KHAU
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(request);
        
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }
    
    // (Sau này chúng ta sẽ thêm endpoint GET /api/users/me để lấy thông tin user)
}