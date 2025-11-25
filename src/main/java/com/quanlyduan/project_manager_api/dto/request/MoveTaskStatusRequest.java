// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/MoveTaskStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskStatusRequest {
    
    @NotNull(message = "ID trạng thái mới không được để trống") // Đã dịch
    private Integer newStatusId;

    // *** BỔ SUNG TRƯỜNG NÀY ĐỂ SỬA LỖI ***
    // Vị trí mong muốn trong cột mới (0, 1, 2...). 
    // Nếu null -> Mặc định thêm vào cuối cột.
    private Integer newSortOrder;
}