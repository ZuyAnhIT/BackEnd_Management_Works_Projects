package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Mức độ ưu tiên của Task (công việc).
 * Enum này khớp với cấu hình CSDL: priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT').
 */
public enum TaskPriority {

    // ==========================================
    // ENUM VALUES (Giá trị mức độ ưu tiên)
    // ==========================================

    /** Mức độ ưu tiên thấp (Có thể xử lý khi rảnh rỗi) */
    LOW,

    /** Mức độ ưu tiên trung bình (Trạng thái mặc định) */
    MEDIUM,

    /** Mức độ ưu tiên cao (Cần hoàn thành sớm) */
    HIGH,

    /** Mức độ khẩn cấp (Cần xử lý ngay lập tức) */
    URGENT

}