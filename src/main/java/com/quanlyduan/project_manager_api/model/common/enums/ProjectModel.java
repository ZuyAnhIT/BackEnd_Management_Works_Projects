package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các mô hình quản lý dự án chính.
 * Tương ứng với ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID') trong Cơ sở dữ liệu.
 */
public enum ProjectModel {

    // ==========================================
    // ENUM VALUES (Giá trị mô hình)
    // ==========================================

    /** Mô hình phát triển lặp đi lặp lại theo Sprint (Agile) */
    SCRUM,

    /** Mô hình quản lý luồng công việc trực quan (Agile) */
    KANBAN,

    /** Mô hình phát triển tuần tự, tuyến tính (Truyền thống) */
    WATERFALL,

    /** Mô hình kết hợp linh hoạt giữa Agile và Truyền thống */
    HYBRID

}