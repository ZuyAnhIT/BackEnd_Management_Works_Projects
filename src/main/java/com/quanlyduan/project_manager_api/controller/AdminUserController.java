package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.AdminUserResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.service.AdminUserService;

/**
 * Controller quan ly nguoi dung toan cuc danh cho System Admin.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIR = "desc";

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Lay danh sach va tim kiem nguoi dung he thong.
     */
    @GetMapping
    @PreAuthorize("@securityService.hasSystemPermission('user:view')") // Sửa thành user:view
    public ResponseEntity<ApiResponse<PageResponseDTO<AdminUserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<AdminUserResponse> response = adminUserService.searchUsers(
                keyword, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success("Retrieved global users successfully.", response));
    }

    /**
     * Thay doi trang thai cua mot nguoi dung (Ban, Unban).
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("@securityService.hasSystemPermission('user:edit')") // Sửa thành user:edit
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable Integer userId,
            @RequestParam String status) {

        adminUserService.changeUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("User status changed to " + status + " successfully.", null));
    }

    /**
     * Cap hoac thu hoi quyen Quan tri vien He thong (SYSTEM_ADMIN).
     */
    @PutMapping("/{userId}/roles/system-admin")
    @PreAuthorize("@securityService.hasSystemPermission('user:edit')") // Sửa thành user:edit
    public ResponseEntity<ApiResponse<Void>> toggleSystemAdminRole(
            @PathVariable Integer userId,
            @RequestParam boolean assign) {

        adminUserService.toggleSystemAdminRole(userId, assign);
        String action = assign ? "assigned to" : "revoked from";
        return ResponseEntity.ok(ApiResponse.success("SYSTEM_ADMIN role " + action + " the user successfully.", null));
    }
}