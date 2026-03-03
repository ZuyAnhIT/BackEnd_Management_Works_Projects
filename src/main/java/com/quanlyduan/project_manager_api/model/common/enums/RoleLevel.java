package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa cấp độ phân cấp của một vai trò (Role).
 * Cấp độ này xác định phạm vi quyền hạn (Scope) của một người dùng trong hệ thống.
 */
public enum RoleLevel {

    // ==========================================
    // ROLE LEVELS (Cấp độ phân cấp)
    // ==========================================

    /** Cấp Hệ thống: Có hiệu lực trên toàn bộ ứng dụng (ví dụ: System Admin) */
    SYSTEM,

    /** Cấp Công ty: Có hiệu lực trong phạm vi một Công ty cụ thể */
    COMPANY,

    /** Cấp Workspace: Có hiệu lực trong một Không gian làm việc cụ thể */
    WORKSPACE,

    /** Cấp Dự án: Có hiệu lực hẹp nhất, chỉ trong một Dự án cụ thể */
    PROJECT

}