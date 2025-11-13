// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateMemberStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private MemberStatus newStatus; // (Phải là ACTIVE hoặc SUSPENDED)
}