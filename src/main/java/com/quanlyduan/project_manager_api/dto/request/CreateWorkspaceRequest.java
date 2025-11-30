// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một Không gian làm việc (Workspace) mới.
 */
@Data
public class CreateWorkspaceRequest {

    // Tên không gian làm việc (Bắt buộc, không được để trống)
    @NotBlank(message = "Workspace name must not be blank")
    private String workspaceName;

    // Mô tả chi tiết về không gian làm việc (Tùy chọn)
    private String description;

    // Đường dẫn URL của ảnh bìa (Tùy chọn - thường là link ảnh đã upload hoặc ảnh mẫu)
    private String coverImage;

    // Mã màu đại diện cho không gian làm việc (Tùy chọn - ví dụ: #3498db)
    private String color;
}