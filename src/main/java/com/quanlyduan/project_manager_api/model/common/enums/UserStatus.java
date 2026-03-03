package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái hoạt động của tài khoản người dùng trong hệ thống.
 */
public enum UserStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái tài khoản)
    // ==========================================

    /** 'Hoạt động' - Người dùng có thể đăng nhập và sử dụng mọi dịch vụ được cấp quyền */
    ACTIVE,

    /** * 'Tạm khóa' - Tài khoản bị vô hiệu hóa quyền truy cập tạm thời.
     * Nguyên nhân: Nhập sai mật khẩu quá nhiều lần hoặc bị Admin khóa do vi phạm.
     */
    LOCKED,

    /** * 'Đã xóa' - Tài khoản không còn tồn tại trong logic nghiệp vụ (Soft Delete).
     * Dữ liệu vẫn được giữ lại trong DB để đảm bảo tính toàn vẹn của lịch sử hệ thống.
     */
    DELETED

}