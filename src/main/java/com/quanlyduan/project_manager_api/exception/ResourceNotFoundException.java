package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoại lệ được ném ra khi một tài nguyên yêu cầu không tồn tại trong hệ thống.
 * Kết quả trả về cho Client sẽ kèm theo mã lỗi HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Khai báo hằng số thông báo lỗi mặc định
    private static final String DEFAULT_MESSAGE = "The requested resource was not found.";

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi mặc định.
     */
    public ResourceNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi tùy chỉnh.
     * @param message Thông báo chi tiết về tài nguyên không tìm thấy.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}