// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Epic.
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào hệ thống.
 */
@Data
public class UpdateEpicRequest {
    
    // Tên Epic mới (Tùy chọn, tối đa 255 ký tự)
    @Size(max = 255, message = "Epic name must not exceed 255 characters")
    private String name;
    
    // Mô tả chi tiết mới (Tùy chọn, tối đa 1000 ký tự)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    // Mã màu hiển thị mới (Tùy chọn - ví dụ: #8e44ad)
    private String color; 
    
    // Trạng thái mới của Epic (Tùy chọn)
    // Giá trị mong đợi: "OPEN", "IN_PROGRESS", "COMPLETED", "CLOSED"
    // Service sẽ parse chuỗi này sang Enum tương ứng.
    private String status; 
    
    // Ngày bắt đầu dự kiến mới
    private LocalDate startDate;

    // Ngày kết thúc dự kiến mới
    private LocalDate dueDate;
}