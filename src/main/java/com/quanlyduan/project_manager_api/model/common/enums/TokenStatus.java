package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Token (ví dụ: Refresh Token, Reset Password Token, Email Verification Token).
 * Kiểm soát tính hợp lệ của Token trong các quá trình xác thực và bảo mật.
 */
public enum TokenStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái Token)
    // ==========================================

    /** 'Hoạt động' - Token đang có hiệu lực và có thể sử dụng để xác thực */
    ACTIVE,

    /** * 'Đã thu hồi' - Token bị vô hiệu hóa chủ động bởi người dùng hoặc hệ thống.
     * Ví dụ: Khi người dùng đăng xuất (Logout) hoặc thực hiện đổi mật khẩu.
     */
    REVOKED,

    /** 'Hết hạn' - Token không còn hiệu lực do đã vượt quá thời gian sống (TTL) quy định */
    EXPIRED

}