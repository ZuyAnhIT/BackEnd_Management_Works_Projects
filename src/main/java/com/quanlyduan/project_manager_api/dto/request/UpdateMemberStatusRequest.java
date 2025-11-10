// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateMemberStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberStatusRequest {

    @NotNull(message = "New status must not be null") // Đã dịch
    private MemberStatus newStatus; // (Phải là ACTIVE hoặc SUSPENDED)
}