// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/UserProfileResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;

// DTO chính: "File JSON khổng lồ"
@Data
@Builder
public class UserProfileResponse {
    // 1. Thông tin cơ bản
    private Integer id;
    private String fullName; // Đã dịch
    private String email;
    private String avatarUrl; // Đã dịch

    private String phoneNumber; // Đã dịch
    private Gender gender; // Đã dịch
    private LocalDate dateOfBirth; // Đã dịch
    
    // 2. Vai trò cấp Hệ thống
    private List<String> systemRoles; // (vd: ["SYSTEM_ADMIN"])
    
    // 3. Vai trò cấp Công ty
    private List<CompanyMembershipDTO> companyMemberships;
    
    // 4. Vai trò cấp Không gian
    private List<WorkspaceMembershipDTO> workspaceMemberships;
    private List<ProjectMembershipDTO> projectMemberships;
    // (Sau này có thể thêm cấp Dự án)
}