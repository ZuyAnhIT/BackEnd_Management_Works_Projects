package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

// DTO chính: "File JSON khổng lồ"
@Data
@Builder
public class UserProfileResponse {
    // 1. Thông tin cơ bản
    private Integer id;
    private String hoTen;
    private String email;
    private String anhDaiDien;
    
    // 2. Vai trò cấp Hệ thống
    private List<String> systemRoles; // (vd: ["SYSTEM_ADMIN"])
    
    // 3. Vai trò cấp Công ty
    private List<CompanyMembershipDTO> companyMemberships;
    
    // 4. Vai trò cấp Không gian
    private List<WorkspaceMembershipDTO> workspaceMemberships;
    
    // (Sau này có thể thêm cấp Dự án)
}
