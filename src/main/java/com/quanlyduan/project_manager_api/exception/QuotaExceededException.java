package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception ném ra khi người dùng hoặc Công ty thực hiện thao tác
 * vượt quá giới hạn tài nguyên của Gói cước (Subscription Plan) hiện tại.
 * Mã lỗi 402 yêu cầu Frontend hiển thị popup "Nâng cấp gói cước".
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}