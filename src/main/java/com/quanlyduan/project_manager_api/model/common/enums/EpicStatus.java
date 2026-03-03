package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của Epic trong hệ thống quản lý dự án.
 * Tương ứng với ENUM('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED') trong Cơ sở dữ liệu.
 */
public enum EpicStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái)
    // ==========================================

    /** Đã mở, sẵn sàng để bắt đầu công việc */
    OPEN,

    /** Đang được triển khai */
    IN_PROGRESS,

    /** Đã hoàn thành (Công việc đã xong, nhưng có thể chưa được kiểm tra cuối cùng) */
    COMPLETED,

    /** Đã đóng (Hoàn tất mọi quy trình, không cho phép thay đổi thêm) */
    CLOSED

}