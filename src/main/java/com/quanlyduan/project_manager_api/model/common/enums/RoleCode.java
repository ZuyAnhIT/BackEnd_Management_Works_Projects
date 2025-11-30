// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/RoleCode.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa các mã vai trò (maRole) chuẩn trong hệ thống.
 * Tên của Enum (viết hoa) phải khớp với giá trị của cột 'maRole' trong bảng Role.
 * Đây là các vai trò được phân cấp theo cấp độ (System, Company, Workspace, Project).
 */

public enum RoleCode {
    
    // === Cấp Hệ thống (System Level) ===
    SYSTEM_ADMIN, // Quản trị viên toàn hệ thống
    USER, // Người dùng cơ bản, đã đăng ký

    // === Cấp Công ty (Company Level) ===
    COMPANY_ADMIN, // Quản trị viên của Công ty
    COMPANY_MANAGER, // Quản lý cấp cao của Công ty
    COMPANY_MEMBER, // Thành viên/Nhân viên cơ bản của Công ty

    // === Cấp Workspace (Workspace Level) ===
    WORKSPACE_ADMIN, // Quản trị viên của Workspace
    WORKSPACE_MEMBER, // Thành viên của Workspace

    // === Cấp Dự án (Project Level) ===
    PROJECT_ADMIN,   // Quản trị viên của Dự án
    PROJECT_MEMBER
    
    // (Có thể thêm các vai trò khác như PROJECT_PO, PROJECT_SM, PROJECT_QA khi mở rộng)
}