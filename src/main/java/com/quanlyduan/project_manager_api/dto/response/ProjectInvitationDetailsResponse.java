package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectInvitationDetailsResponse {
    private String email;
    private String projectName;
    private String roleName;
    private boolean accountExists; // true: Hiện form Login, false: Hiện form Register
    private LocalDateTime expiresAt;
}