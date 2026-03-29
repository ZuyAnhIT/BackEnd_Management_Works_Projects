package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoại lệ được ném ra khi tài nguyên sử dụng vượt quá giới hạn của gói cước hiện tại.
 * Trả về mã lỗi HTTP 402 Payment Required để yêu cầu người dùng nâng cấp gói dịch vụ.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class QuotaExceededException extends RuntimeException {

    // Khai báo hằng số thông báo lỗi mặc định
    private static final String DEFAULT_MESSAGE = "Resource quota exceeded. Please upgrade your subscription plan to continue.";

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi mặc định.
     */
    public QuotaExceededException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi tùy chỉnh.
     * @param message Thông báo chi tiết về tài nguyên bị giới hạn.
     */
    public QuotaExceededException(String message) {
        super(message);
    }
}