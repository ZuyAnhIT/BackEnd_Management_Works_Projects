// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RoleUpdateRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật vai trò (Role) của một thành viên.
 * Được sử dụng chung cho các cấp độ: Công ty, Không gian làm việc, và Dự án.
 */
@Data
public class RoleUpdateRequest {
    
    // Mã vai trò mới muốn gán (Bắt buộc)
    // Ví dụ: "COMPANY_ADMIN", "PROJECT_MEMBER", "GUEST_PROJECT"
    @NotBlank(message = "Role code must not be blank")
    private String roleCode;
}