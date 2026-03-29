package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoai le duoc nem ra khi cong ty hoac nguoi dung vuot qua han muc tai nguyen cua goi cuoc.
 * Su dung ma loi HTTP 402 Payment Required de thong bao can nang cap goi dich vu.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class OverageException extends RuntimeException {

    // Khai bao hang so thong bao loi mac dinh
    private static final String DEFAULT_MESSAGE = "Subscription quota exceeded. Please upgrade your plan to continue.";

    /**
     * Khoi tao ngoai le voi thong bao mac dinh.
     */
    public OverageException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Khoi tao ngoai le voi thong bao tuy chinh.
     * @param message Thong bao chi tiet ve tai nguyen bi vuot han muc.
     */
    public OverageException(String message) {
        super(message);
    }
}