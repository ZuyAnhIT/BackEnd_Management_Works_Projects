// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMemberResponse {
    
    private Integer memberId; // ID của bản ghi CongTyThanhVien (hoặc null nếu là PENDING) 
    
    // Thông tin từ NguoiDung (nếu có)
    private Integer userId;
    private String fullName; // Đã dịch
    private String email;
    private String avatarUrl; // Đã dịch
    
    // Thông tin từ Role
    private String roleName; // Tên vai trò (vd: "Quản trị Công ty")
    
    // Thông tin từ CongTyThanhVien (nếu có)
    private String jobTitle; // Đã dịch
    private LocalDateTime joinedAt; // Đã dịch
    
    // Trạng thái kết hợp
    private CombinedMemberStatus status;
}