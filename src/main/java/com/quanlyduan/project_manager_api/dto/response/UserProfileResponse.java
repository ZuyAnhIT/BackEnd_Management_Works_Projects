// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/UserProfileResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;

/**
 * DTO phản hồi thông tin Hồ sơ người dùng đầy đủ.
 * Dùng cho API GET /api/users/me, tổng hợp tất cả thông tin cá nhân và vai trò thành viên
 * từ các bảng khác nhau (users, user_roles, company_members, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    // ========================================================================
    // 1. ĐỊNH DANH & HỆ THỐNG (IDENTITY & AUDIT)
    // ========================================================================
    
    private Integer id;
    private String email;
    
    // Trạng thái tài khoản (ACTIVE, LOCKED, DELETED)
    private UserStatus status;
    private boolean isEmailVerified;

    // Thời điểm tạo tài khoản và lần đăng nhập cuối
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // ========================================================================
    // 2. THÔNG TIN CÁ NHÂN (PERSONAL DETAILS)
    // ========================================================================

    private String fullName;
    private String avatarUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;

    // ========================================================================
    // 3. VAI TRÒ HỆ THỐNG (SYSTEM ROLES)
    // ========================================================================

    // Danh sách các mã vai trò cấp Hệ thống (ví dụ: ["SYSTEM_ADMIN", "USER"])
    private List<String> systemRoles;

    // ========================================================================
    // 4. TƯ CÁCH THÀNH VIÊN THEO CẤP BẬC (HIERARCHY MEMBERSHIPS)
    // ========================================================================

    // Danh sách tư cách thành viên trong các Công ty (Bao gồm Role và Company Info)
    private List<CompanyMembershipDTO> companyMemberships;

    // Danh sách tư cách thành viên trong các Workspace
    private List<WorkspaceMembershipDTO> workspaceMemberships;

    // Danh sách tư cách thành viên trong các Project
    private List<ProjectMembershipDTO> projectMemberships;
}