package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa các mã vai trò (maRole) chuẩn trong hệ thống.
 * Tên của Enum (viết hoa) phải khớp với giá trị của cột 'maRole' trong bảng Role.
 */

public enum RoleCode {
    
    // === Cấp Hệ thống ===
    SYSTEM_ADMIN,  // Admin toàn hệ thống
    USER,          // Người dùng cơ bản

    // === Cấp Công ty ===
    COMPANY_ADMIN,   // Quản trị viên công ty
    COMPANY_MANAGER, // Quản lý công ty (ví dụ)
    COMPANY_MEMBER,  // Thành viên công ty

    // === Cấp Workspace ===
    WORKSPACE_ADMIN,
    WORKSPACE_MEMBER,

    // === Cấp Dự án ===
    PROJECT_ADMIN,
    PROJECT_MEMBER
    
    // (Thêm các role khác như PROJECT_PO, PROJECT_SM... khi cần)
}