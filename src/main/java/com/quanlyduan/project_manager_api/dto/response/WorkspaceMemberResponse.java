// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    
    private Integer memberId; // ID của bản ghi WorkspaceMember

    // Thông tin User
    private Integer userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    
    // Thông tin Role
    private String roleName; // Tên vai trò (vd: "Quản trị Không gian")
    
    // Thông tin thành viên
    private LocalDateTime joinedAt;
    private MemberStatus status; // (ACTIVE, REMOVED)
}