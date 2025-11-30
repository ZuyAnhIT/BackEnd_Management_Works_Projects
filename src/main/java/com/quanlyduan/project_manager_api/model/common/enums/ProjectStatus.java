// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/ProjectStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các trạng thái chính của một Dự án trong vòng đời của nó.
 */
public enum ProjectStatus {
    ACTIVE,      // Đang hoạt động/mở, sẵn sàng cho công việc.
    NEW,         // Mới tạo, chưa được bắt đầu hoặc phân bổ.
    IN_PROGRESS, // Đang tiến hành công việc.
    PAUSED,      // Tạm dừng (chưa hoàn thành nhưng công việc bị gián đoạn).
    COMPLETED,   // Đã hoàn thành (mục tiêu đã đạt được).
    CANCELLED    // Đã bị hủy bỏ/không còn hiệu lực (Soft delete).
}