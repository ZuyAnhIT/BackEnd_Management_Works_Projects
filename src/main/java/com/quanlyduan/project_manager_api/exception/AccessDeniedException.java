package com.quanlyduan.project_manager_api.exception;

// Spring Framework
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được ném ra khi một người dùng (đã xác thực thành công)
 * cố gắng thực hiện hành động hoặc truy cập vào tài nguyên mà họ không có quyền hạn (Authorization).
 * * Ví dụ: Một Member cố gắng xóa Project của Admin.
 * Kết quả trả về cho Client sẽ có mã HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    /**
     * Khởi tạo exception với thông báo lỗi mặc định.
     */
    public AccessDeniedException() {
        super("You do not have permission to access this resource.");
    }

    /**
     * Khởi tạo exception với thông báo lỗi tùy chỉnh.
     * @param message Thông báo chi tiết về nguyên nhân bị từ chối truy cập.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}