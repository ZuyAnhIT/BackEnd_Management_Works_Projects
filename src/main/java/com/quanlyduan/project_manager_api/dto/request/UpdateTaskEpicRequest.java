// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật Epic của một Công việc (Task).
 * Được sử dụng trong các thao tác kéo thả Task vào/ra khỏi Panel Epic trên giao diện.
 */
@Data
public class UpdateTaskEpicRequest {
    
    // ID của Epic mục tiêu.
    // - Nếu có giá trị: Gán Task vào Epic đó.
    // - Nếu là null: Gỡ Task khỏi Epic hiện tại (Unassign).
    private Integer epicId;
}