// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/GoogleLoginRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    
    @NotBlank(message = "Mã thông báo Google không được để trống") // Đã dịch
    private String googleToken; // Đây sẽ là id_token
}