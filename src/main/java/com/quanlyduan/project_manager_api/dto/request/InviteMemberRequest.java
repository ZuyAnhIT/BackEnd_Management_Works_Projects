// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteMemberRequest.java
// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull; // Bỏ
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank(message = "Email không được để trống") // Đã dịch
    @Email(message = "Email không đúng định dạng") // Đã dịch
    private String email;

    @NotBlank(message = "Mã vai trò không được để trống") // Đã dịch
    private String roleCode; // Đã dịch (Thay cho roleId)
}