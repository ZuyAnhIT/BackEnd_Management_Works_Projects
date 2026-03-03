package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái thành viên trong một mối quan hệ (Công ty, Workspace, Dự án).
 */
public enum MemberStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái)
    // ==========================================

    /** Thành viên đang hoạt động bình thường và có quyền truy cập */
    ACTIVE,

    /** Bị tạm dừng quyền truy cập (Ví dụ: đang nghỉ phép, vi phạm quy định) */
    SUSPENDED,

    /** Đã bị xóa bởi Admin hoặc tự ý rời khỏi tổ chức/nhóm */
    REMOVED

}