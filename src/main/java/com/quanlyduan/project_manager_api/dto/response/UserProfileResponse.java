// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/UserProfileResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;

// DTO chính: "File JSON khổng lồ"
@Data
@Builder
public class UserProfileResponse {
    // 1. Thông tin cơ bản
    private Integer id;
    private String fullName; // Đã dịch
    private String email;
    private String avatarUrl; // Đã dịch
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private UserStatus status;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    // 2. Vai trò cấp Hệ thống
    private List<String> systemRoles; // (vd: ["SYSTEM_ADMIN"])
    
    // 3. Vai trò cấp Công ty
    private List<CompanyMembershipDTO> companyMemberships;
    
    // 4. Vai trò cấp Không gian
    private List<WorkspaceMembershipDTO> workspaceMemberships;
    private List<ProjectMembershipDTO> projectMemberships;
    // (Sau này có thể thêm cấp Dự án)
}