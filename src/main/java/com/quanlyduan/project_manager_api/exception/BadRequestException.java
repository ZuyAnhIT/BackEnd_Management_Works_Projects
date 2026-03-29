package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoại lệ được ném ra khi yêu cầu từ phía người dùng không hợp lệ hoặc vi phạm quy tắc nghiệp vụ.
 * Kết quả trả về cho Client sẽ kèm theo mã lỗi HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    // Khai báo hằng số thông báo lỗi mặc định
    private static final String DEFAULT_MESSAGE = "The request is invalid or cannot be processed.";

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi mặc định.
     */
    public BadRequestException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi tùy chỉnh.
     * @param message Thông báo chi tiết về nguyên nhân dữ liệu không hợp lệ.
     */
    public BadRequestException(String message) {
        super(message);
    }
}