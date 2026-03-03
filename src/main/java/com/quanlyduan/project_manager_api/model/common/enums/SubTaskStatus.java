package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Subtask (công việc con) thuộc Task.
 */
public enum SubTaskStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái Subtask)
    // ==========================================

    /** Cần làm (Chưa bắt đầu thực hiện) */
    TO_DO,

    /** Đang trong quá trình thực hiện */
    IN_PROGRESS,

    /** Đã hoàn thành công việc con */
    DONE

}