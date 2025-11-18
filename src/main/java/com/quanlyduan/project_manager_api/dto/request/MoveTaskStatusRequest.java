// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/MoveTaskStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskStatusRequest {
    @NotNull(message = "ID trạng thái mới không được để trống") // Đã dịch
    private Integer newStatusId;
}