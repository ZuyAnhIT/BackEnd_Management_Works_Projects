// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/RoleLevel.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa cấp độ phân cấp của một vai trò (Role).
 * Cấp độ này xác định phạm vi quyền hạn của một người dùng trong hệ thống.
 */
public enum RoleLevel {
    SYSTEM,     // Cấp Hệ thống (Phạm vi toàn bộ ứng dụng)
    COMPANY,    // Cấp Công ty (Phạm vi trong một Công ty cụ thể)
    WORKSPACE,  // Cấp Workspace (Phạm vi trong một Không gian làm việc cụ thể)
    PROJECT     // Cấp Dự án (Phạm vi trong một Dự án cụ thể)
}