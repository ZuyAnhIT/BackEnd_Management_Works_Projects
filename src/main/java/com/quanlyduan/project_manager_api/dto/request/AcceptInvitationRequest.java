package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO dùng để nhận yêu cầu chấp nhận lời mời.
 * Được sử dụng chung cho cả quy trình mời vào Công ty, Workspace và Dự án.
 */
@Data
public class AcceptInvitationRequest {

    // ==========================================
    // REQUEST DATA
    // ==========================================

    /**
     * Chuỗi Token định danh lời mời.
     * (Thường được trích xuất từ URL đính kèm trong Email gửi đến người dùng).
     */
    @NotBlank(message = "Invitation token must not be blank")
    private String invitationToken;

}