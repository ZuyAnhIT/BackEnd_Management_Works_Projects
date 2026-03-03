package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các trạng thái chính của một Dự án trong vòng đời của nó.
 */
public enum ProjectStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái dự án)
    // ==========================================

    /** Mới tạo, đang trong giai đoạn lập kế hoạch hoặc chờ phê duyệt */
    NEW,

    /** Đang hoạt động/mở, sẵn sàng cho công việc (Sử dụng cho Workspace/Global view) */
    ACTIVE,

    /** Đang trong quá trình thực thi các Task/Sprints */
    IN_PROGRESS,

    /** Tạm dừng (Dự án chưa xong nhưng công việc bị gián đoạn tạm thời) */
    PAUSED,

    /** Đã hoàn thành mọi mục tiêu đề ra */
    COMPLETED,

    /** Đã bị hủy bỏ hoặc không còn giá trị triển khai (Sử dụng cho Soft Delete) */
    CANCELLED

}