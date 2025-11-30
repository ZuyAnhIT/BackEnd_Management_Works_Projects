// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateTagRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Thẻ (Tag) trong dự án.
 * Tag được dùng để gắn nhãn và phân loại các công việc (Task).
 */
@Data
public class CreateTagRequest {

    // Tên thẻ (Bắt buộc, không được để trống)
    @NotBlank(message = "Tag name must not be blank")
    private String name;

    // Mã màu HEX hiển thị (Tùy chọn - ví dụ: #FF5733)
    private String color;

    // Mô tả chi tiết về ý nghĩa của thẻ (Tùy chọn)
    private String description;
}