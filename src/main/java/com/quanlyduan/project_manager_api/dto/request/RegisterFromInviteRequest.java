// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterFromInviteRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterFromInviteRequest {
    @NotBlank(message = "Họ và tên không được để trống") // Đã dịch
    private String fullName; // Đã dịch

    @NotBlank(message = "Mật khẩu không được để trống") // Đã dịch
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự") // Đã dịch
    private String password; // Đã dịch

    @NotBlank(message = "Mã mời không được để trống") // Đã dịch
    private String invitationToken;
}