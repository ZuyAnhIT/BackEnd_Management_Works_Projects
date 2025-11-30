// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO nhận dữ liệu khi tạo mới một Epic.
 */
@Data
public class CreateEpicRequest {
    
    // Tên Epic (Bắt buộc, không quá 255 ký tự)
    @NotBlank(message = "Epic name must not be blank")
    @Size(max = 255, message = "Epic name must not exceed 255 characters")
    private String name;
    
    // Mô tả chi tiết (Tùy chọn, không quá 1000 ký tự)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    // Mã màu HEX (Tùy chọn). Nếu null, hệ thống sẽ tự động chọn màu ngẫu nhiên.
    private String color; 
    
    // Ngày bắt đầu dự kiến
    private LocalDate startDate;

    // Ngày kết thúc dự kiến
    private LocalDate dueDate;
}