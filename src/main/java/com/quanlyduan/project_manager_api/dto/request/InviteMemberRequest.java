// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank(message = "Email must not be blank") // Đã dịch
    @Email(message = "Email is not in a valid format") // Đã dịch
    private String email;

    @NotNull(message = "Role ID must not be null") // Đã dịch
    private Integer roleId;
}