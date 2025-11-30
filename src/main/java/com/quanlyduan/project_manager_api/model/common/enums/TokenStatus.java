// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/TokenStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Token (ví dụ: Refresh Token, Reset Password Token, Email Verification Token).
 */
public enum TokenStatus {
    ACTIVE,     // 'Hoạt động' (Token đang có hiệu lực)
    REVOKED,    // 'Đã thu hồi' (Token đã bị vô hiệu hóa bởi hành động người dùng/hệ thống, ví dụ: logout, đổi mật khẩu)
    EXPIRED     // 'Hết hạn' (Token hết hiệu lực do vượt quá thời gian cho phép)
}