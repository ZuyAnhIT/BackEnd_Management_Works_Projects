package com.quanlyduan.project_manager_api.exception;

// Spring Framework
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được ném ra khi yêu cầu từ Client không hợp lệ hoặc vi phạm quy tắc nghiệp vụ.
 * Ví dụ: 
 * - Gửi dữ liệu sai định dạng mà Validation chưa bắt được.
 * - Thực hiện hành động không cho phép (Ví dụ: Thêm Task vào một Sprint đã kết thúc).
 * Kết quả trả về cho Client sẽ có mã HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * Khởi tạo exception với thông báo mặc định.
     */
    public BadRequestException() {
        super("The request is invalid or cannot be processed.");
    }

    /**
     * Khởi tạo exception với thông báo tùy chỉnh.
     * @param message Thông báo chi tiết về lỗi nghiệp vụ hoặc dữ liệu không hợp lệ.
     */
    public BadRequestException(String message) {
        super(message);
    }
}