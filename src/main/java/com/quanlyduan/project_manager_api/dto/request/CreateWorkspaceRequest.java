package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một Không gian làm việc (Workspace) mới.
 */
@Data
public class CreateWorkspaceRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Workspace)
    // ==========================================

    /**
     * Tên không gian làm việc.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Workspace name must not be blank")
    private String workspaceName;

    /**
     * Mô tả chi tiết về mục đích hoặc nội dung của không gian làm việc.
     * (Tùy chọn)
     */
    private String description;

    /**
     * Đường dẫn URL của ảnh bìa (Cover Image).
     * (Tùy chọn - Thường là link ảnh đã upload lên cloud storage hoặc ảnh mẫu có sẵn do hệ thống cung cấp).
     */
    private String coverImage;

    /**
     * Mã màu HEX đại diện cho không gian làm việc để hiển thị trên UI.
     * (Tùy chọn - Ví dụ: #3498db, #e74c3c).
     */
    private String color;

}