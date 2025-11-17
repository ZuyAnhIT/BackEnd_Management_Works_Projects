// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectMemberResponse {
    private Integer memberId; // ID của bản ghi ProjectMember
    private Integer userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String roleName; // Tên vai trò (vd: "Project Admin")
    private LocalDateTime joinedAt;
    private MemberStatus status;
}