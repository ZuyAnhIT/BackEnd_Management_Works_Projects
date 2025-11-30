// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/UserStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái hoạt động của tài khoản người dùng trong hệ thống.
 */
public enum UserStatus {
    ACTIVE,     // 'Hoạt động' (Người dùng có thể đăng nhập và sử dụng dịch vụ)
    LOCKED,     // 'Tạm khóa' (Tài khoản bị khóa do quá nhiều lần đăng nhập sai, hoặc bị Admin khóa tạm thời)
    DELETED     // 'Đã xóa' (Xóa mềm - Soft Delete)
}