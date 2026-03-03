package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Sprint trong quy trình Scrum/Agile.
 * Tương ứng với ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') trong bảng sprints.
 */
public enum SprintStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái Sprint)
    // ==========================================

    /** Chưa bắt đầu, đang ở trạng thái chuẩn bị (Planned) */
    NOT_STARTED,

    /** Đang diễn ra, công việc đang được thực hiện (Active Sprint) */
    IN_PROGRESS,

    /** Đã hoàn thành và đóng Sprint (Done) */
    COMPLETED,

    /** Đã bị hủy bỏ trước khi thời hạn Sprint kết thúc */
    CANCELLED

}