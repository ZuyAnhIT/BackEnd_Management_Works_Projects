package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    @NotBlank(message = "Invitation token không được để trống")
    private String invitationToken;
}
