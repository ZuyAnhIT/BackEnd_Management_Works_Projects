// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectInvitationResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectInvitationResponse {
    private Integer id;             // ID lời mời (để hủy/gửi lại)
    private String email;           // Email người được mời
    private String roleCode;        // Vai trò dự kiến (MEMBER, GUEST...)
    private String status;          // PENDING, EXPIRED...
    private LocalDateTime invitedAt; // Thời gian mời
    
    // Thông tin người mời (Optional - để biết ai là người gửi)
    private String inviterName;
    private String inviterAvatar;
}