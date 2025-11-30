// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateSubTaskStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật RIÊNG trạng thái của SubTask.
 * Thường được sử dụng cho các thao tác nhanh (Quick Action) như:
 * - Kéo thả trên giao diện.
 * - Tích chọn hoàn thành (Check-box).
 * Giúp giảm tải dữ liệu so với việc dùng UpdateSubTaskRequest đầy đủ.
 */
@Data
public class UpdateSubTaskStatusRequest {

    // Trạng thái mới muốn áp dụng (Enum: TO_DO, IN_PROGRESS, DONE)
    // Ví dụ JSON payload: { "status": "DONE" }
    private SubTaskStatus status;
}