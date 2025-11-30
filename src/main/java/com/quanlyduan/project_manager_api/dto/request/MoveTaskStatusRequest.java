// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/MoveTaskStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho hành động di chuyển Task sang một cột trạng thái khác (Kéo thả trên Board).
 */
@Data
public class MoveTaskStatusRequest {
    
    // ID của trạng thái (cột) mới mà Task sẽ được chuyển đến (Bắt buộc)
    @NotNull(message = "New status ID must not be null")
    private Integer newStatusId;

    // Vị trí mong muốn của Task trong cột mới (0, 1, 2...)
    // Tùy chọn: Nếu null, hệ thống sẽ mặc định thêm vào cuối cột.
    private Integer newSortOrder;
}