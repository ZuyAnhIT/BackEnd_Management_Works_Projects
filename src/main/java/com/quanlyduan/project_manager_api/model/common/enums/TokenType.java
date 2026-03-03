package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa các loại Token khác nhau được sử dụng trong hệ thống xác thực và bảo mật.
 */
public enum TokenType {

    // ==========================================
    // ENUM VALUES (Các loại Token)
    // ==========================================

    /** Access Token: Dùng để xác thực quyền truy cập tài nguyên (thường có TTL ngắn) */
    ACCESS,

    /** Refresh Token: Dùng để cấp lại Access Token mới mà không cần đăng nhập lại */
    REFRESH,

    /** Reset Password: Token dùng riêng cho luồng nghiệp vụ "Quên mật khẩu" */
    RESET_PASSWORD,

    /** Email Verification: Token gửi qua mail để xác nhận quyền sở hữu tài khoản */
    EMAIL_VERIFICATION,

    /** API Token: Dùng cho việc tích hợp các dịch vụ bên ngoài hoặc công cụ bên thứ ba */
    API

}