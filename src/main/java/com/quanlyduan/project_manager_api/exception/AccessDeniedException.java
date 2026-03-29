package com.quanlyduan.project_manager_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoai le duoc nem ra khi nguoi dung khong co quyen truy cap vao tai nguyen.
 * Tra ve ma loi HTTP 403 Forbidden cho phia Client.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    // Khai bao hang so thong bao loi mac dinh
    private static final String DEFAULT_MESSAGE = "You do not have permission to access this resource.";

    /**
     * Khoi tao voi thong bao loi mac dinh.
     */
    public AccessDeniedException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Khoi tao voi thong bao loi tuy chinh.
     * @param message Thong bao chi tiet ve loi phan quyen.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}