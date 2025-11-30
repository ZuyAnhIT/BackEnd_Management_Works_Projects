// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/EpicStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của Epic trong hệ thống quản lý dự án (ví dụ: Jira, Azure DevOps).
 * Maps to the ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED')
 */
public enum EpicStatus {
    OPEN,        // Đã mở, sẵn sàng để bắt đầu công việc
    IN_PROGRESS, // Đang được triển khai
    COMPLETED,   // Đã hoàn thành (công việc đã xong, nhưng có thể chưa được kiểm tra cuối cùng)
    CLOSED       // Đã đóng (hoàn tất mọi quy trình, không thay đổi)
}