package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Thẻ (Tag) trong dự án.
 * Tag được dùng để gắn nhãn, phân loại và lọc các công việc (Task).
 */
@Data
public class CreateTagRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Thẻ/Nhãn)
    // ==========================================

    /**
     * Tên thẻ (Tag name).
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Tag name must not be blank")
    private String name;

    /**
     * Mã màu HEX hiển thị trên giao diện người dùng.
     * (Tùy chọn - Ví dụ: #FF5733)
     */
    private String color;

    /**
     * Mô tả chi tiết về ý nghĩa hoặc mục đích sử dụng của thẻ.
     * (Tùy chọn)
     */
    private String description;

}