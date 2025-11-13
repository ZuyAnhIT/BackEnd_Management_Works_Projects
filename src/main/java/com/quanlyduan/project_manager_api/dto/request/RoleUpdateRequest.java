// DTO CHO USER STORY 1
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @NotBlank(message = "Mã vai trò không được để trống")
    private String roleCode;
}
