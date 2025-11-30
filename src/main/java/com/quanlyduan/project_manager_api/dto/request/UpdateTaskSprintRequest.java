// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskSprintRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;

/**
 * DTO nhận dữ liệu cho hành động di chuyển Task vào/ra khỏi Sprint (Kéo thả dọc).
 * Hỗ trợ:
 * 1. Chuyển Task từ Backlog vào Sprint.
 * 2. Chuyển Task từ Sprint này sang Sprint khác.
 * 3. Chuyển Task từ Sprint về lại Backlog.
 * 4. Sắp xếp lại vị trí (Sort Order) của Task trong danh sách đích.
 */
@Data
public class UpdateTaskSprintRequest {
    
    // ID của Sprint đích.
    // - Nếu có giá trị (ví dụ: 10): Chuyển Task vào Sprint 10.
    // - Nếu là null: Chuyển Task về Backlog (Gỡ khỏi mọi Sprint).
    private Integer sprintId;

    // Vị trí mong muốn của Task trong danh sách đích (0, 1, 2...).
    // Tùy chọn: Nếu null, hệ thống sẽ mặc định thêm vào cuối danh sách.
    private Integer newSortOrder;
}