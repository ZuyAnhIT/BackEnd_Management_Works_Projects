// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyInvitationResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CompanyInvitationResponse {
    private Integer id;
    private String email;           // Email người được mời
    private String roleName;        // Vai trò dự kiến (vd: COMPANY_MEMBER)
    private String invitedByName;   // Tên người mời (Admin)
    private String status;          // PENDING
    private LocalDateTime expiresAt;// Ngày hết hạn
    private String invitationLink;  // Link mời (để Admin copy gửi lại)
}