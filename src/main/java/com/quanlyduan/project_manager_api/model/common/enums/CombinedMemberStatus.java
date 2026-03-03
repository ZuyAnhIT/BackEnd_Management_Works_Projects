package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái thành viên tổng hợp (dùng cho mục đích hiển thị/DTO).
 * Nó kết hợp cả trạng thái thành viên (ACTIVE, SUSPENDED, REMOVED)
 * và trạng thái lời mời (PENDING).
 */
public enum CombinedMemberStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái)
    // ==========================================
    
    /** Là thành viên và đang "Hoạt động" */
    ACTIVE,

    /** Là thành viên nhưng bị "Tạm dừng" */
    SUSPENDED,

    /** Là thành viên nhưng bị "Xóa/Rời khỏi" */
    REMOVED,

    /** Chưa là thành viên, lời mời đang "Chờ" */
    PENDING

}