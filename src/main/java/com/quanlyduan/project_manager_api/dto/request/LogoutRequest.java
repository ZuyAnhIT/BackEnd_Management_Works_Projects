// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/LogoutRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank(message = "Mã làm mới không được để trống") // Đã dịch
    private String refreshToken;
}