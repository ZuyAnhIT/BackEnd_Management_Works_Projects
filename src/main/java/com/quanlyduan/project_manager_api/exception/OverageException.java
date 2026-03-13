package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// HTTP 402 là mã lỗi chuẩn mực cho việc "Hết tiền/Hết quota" trong các hệ thống API trả phí
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class OverageException extends RuntimeException {
    
    public OverageException(String message) {
        super(message);
    }
}