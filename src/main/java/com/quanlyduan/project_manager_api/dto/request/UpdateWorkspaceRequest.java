// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Không gian làm việc (Workspace).
 * Hỗ trợ cơ chế cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 * - Các trường gửi lên là null sẽ bị bỏ qua, giữ nguyên giá trị cũ trong hệ thống.
 */
@Data
public class UpdateWorkspaceRequest {

    // Tên không gian làm việc mới (Tùy chọn).
    // Nếu được cung cấp, độ dài phải nằm trong khoảng từ 1 đến 255 ký tự.
    @Size(min = 1, max = 255, message = "Workspace name must be between 1 and 255 characters")
    private String name;

    // Mô tả chi tiết mới (Tùy chọn).
    private String description;

    // Đường dẫn ảnh bìa mới (Tùy chọn).
    // Trường này thường chứa URL string hoặc đường dẫn file sau khi upload.
    private String coverImage;

    // Mã màu đại diện mới (Tùy chọn - ví dụ: #3498db).
    private String color;
}