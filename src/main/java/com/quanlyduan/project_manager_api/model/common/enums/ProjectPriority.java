package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Mức độ ưu tiên của Dự án hoặc Task.
 */
public enum ProjectPriority {

    // ==========================================
    // ENUM VALUES (Giá trị mức độ ưu tiên)
    // ==========================================

    /** Mức độ ưu tiên thấp (Có thể xử lý sau cùng) */
    LOW,

    /** Mức độ ưu tiên trung bình (Mặc định cho hầu hết dự án/task) */
    MEDIUM,

    /** Mức độ ưu tiên cao (Cần được chú ý sớm) */
    HIGH,

    /** Mức độ khẩn cấp (Cần xử lý ngay lập tức để tránh đình trệ) */
    URGENT

}