// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RoleUpdateRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    
    @NotBlank(message = "Mã vai trò (roleCode) không được để trống")
    private String roleCode;
}