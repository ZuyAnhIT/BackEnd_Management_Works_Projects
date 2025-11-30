// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/TokenType.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Định nghĩa các loại Token khác nhau được sử dụng trong hệ thống xác thực.
 */
public enum TokenType {
    ACCESS,             // Access Token (dùng để truy cập tài nguyên, thường có thời gian sống ngắn)
    REFRESH,            // Refresh Token (dùng để lấy Access Token mới, thời gian sống dài hơn)
    RESET_PASSWORD,     // Token Đặt lại Mật khẩu (dùng cho quy trình quên mật khẩu)
    EMAIL_VERIFICATION, // Token Xác minh Email (dùng để kích hoạt tài khoản)
    API                 // API Token (dùng cho các dịch vụ bên ngoài, tích hợp)
}