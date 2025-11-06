// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/AcceptInvitationRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    @NotBlank(message = "Invitation token must not be blank") // Đã dịch
    private String invitationToken;
}