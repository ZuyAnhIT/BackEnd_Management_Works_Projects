package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái hoạt động của Công ty.
 */
public enum CompanyStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái)
    // ==========================================

    /** 'Hoạt động' bình thường */
    ACTIVE,

    /** 'Bị tạm dừng' (Ví dụ: hết hạn hợp đồng, vi phạm quy định) */
    SUSPENDED,

    /** 'Đã xóa' (Xóa mềm - Soft Delete) */
    DELETED

}