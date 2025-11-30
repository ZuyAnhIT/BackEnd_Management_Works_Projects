// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/AcceptInvitationRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO dùng để nhận yêu cầu chấp nhận lời mời.
 * Được sử dụng chung cho cả quy trình mời vào Công ty và Dự án.
 */
@Data
public class AcceptInvitationRequest {
    
    // Chuỗi Token định danh lời mời (thường được lấy từ URL trong email)
    @NotBlank(message = "Invitation token must not be blank")
    private String invitationToken;
}