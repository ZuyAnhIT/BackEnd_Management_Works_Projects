// File: src/main/java/com/quanlyduan/project_manager_api/service/EmailService.java
package com.quanlyduan.project_manager_api.service;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến việc gửi Email.
 * Thường được sử dụng để gửi thông báo, xác thực tài khoản, hoặc đặt lại mật khẩu.
 */
public interface EmailService {
    
    /**
     * Gửi email cơ bản.
     * @param to Địa chỉ email người nhận.
     * @param subject Tiêu đề của email.
     * @param body Nội dung của email (có thể là HTML hoặc plain text).
     */
    void sendEmail(String to, String subject, String body);
}