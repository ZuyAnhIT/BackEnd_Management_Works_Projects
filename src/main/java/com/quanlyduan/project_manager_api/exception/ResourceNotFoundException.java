package com.quanlyduan.project_manager_api.exception;

// Spring Framework
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được ném ra khi một tài nguyên (Entity) yêu cầu không tồn tại trong hệ thống.
 * Ví dụ: 
 * - Tìm kiếm một Task bằng ID nhưng không có trong Database.
 * - Truy cập vào một Workspace đã bị xóa vĩnh viễn.
 * Kết quả trả về cho Client sẽ có mã HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Khởi tạo exception với thông báo lỗi mặc định.
     */
    public ResourceNotFoundException() {
        super("The requested resource was not found.");
    }

    /**
     * Khởi tạo exception với thông báo lỗi tùy chỉnh.
     * @param message Thông báo chi tiết về tài nguyên không tìm thấy (VD: "Task not found with id: 101").
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}