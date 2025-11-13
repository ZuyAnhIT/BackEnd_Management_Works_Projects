// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/AcceptInvitationRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    @NotBlank(message = "Mã mời không được để trống") // Đã dịch
    private String invitationToken;
}