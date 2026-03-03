package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa các mã vai trò (Role Code) chuẩn trong hệ thống.
 * Tên của Enum phải khớp với giá trị của cột 'role_code' trong bảng Role.
 * Các vai trò được phân cấp theo phạm vi ảnh hưởng (System, Company, Workspace, Project).
 */
public enum RoleCode {

    // ==========================================
    // SYSTEM LEVEL (Cấp Hệ thống)
    // ==========================================
    /** Quản trị viên toàn hệ thống, có quyền trên tất cả các Company */
    SYSTEM_ADMIN, 

    /** Người dùng cơ bản đã đăng ký tài khoản nhưng chưa thuộc tổ chức nào */
    USER, 

    // ==========================================
    // COMPANY LEVEL (Cấp Công ty)
    // ==========================================
    /** Quản trị viên của Công ty, có toàn quyền trong phạm vi công ty đó */
    COMPANY_ADMIN, 

    /** Quản lý cấp cao của Công ty, tham gia điều hành các Workspace */
    COMPANY_MANAGER, 

    /** Thành viên hoặc nhân viên cơ bản của Công ty */
    COMPANY_MEMBER, 

    // ==========================================
    // WORKSPACE LEVEL (Cấp Workspace)
    // ==========================================
    /** Quản trị viên của một Workspace cụ thể */
    WORKSPACE_ADMIN, 

    /** Thành viên được tham gia làm việc trong một Workspace */
    WORKSPACE_MEMBER, 

    // ==========================================
    // PROJECT LEVEL (Cấp Dự án)
    // ==========================================
    /** Quản trị viên của Dự án, quản lý Sprint/Epic/Task */
    PROJECT_ADMIN,   

    /** Thành viên thực hiện các Task trong Dự án */
    PROJECT_MEMBER

    // Gợi ý mở rộng: PROJECT_PO (Product Owner), PROJECT_SM (Scrum Master), PROJECT_QA
}