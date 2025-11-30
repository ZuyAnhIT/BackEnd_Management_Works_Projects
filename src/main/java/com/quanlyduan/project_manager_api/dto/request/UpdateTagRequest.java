// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTagRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Thẻ (Tag).
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 */
@Data
public class UpdateTagRequest {

    // Tên mới của thẻ (Tùy chọn)
    private String name;

    // Mã màu HEX mới (Tùy chọn - ví dụ: #00AA00)
    private String color;

    // Mô tả chi tiết mới (Tùy chọn)
    private String description;
}