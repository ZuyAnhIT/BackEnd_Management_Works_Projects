// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateProjectRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Dự án.
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 */
@Data
public class UpdateProjectRequest {
    
    // Tên dự án mới (Tùy chọn, độ dài từ 1-255 ký tự)
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    // Mã dự án mới (Tùy chọn - Service sẽ kiểm tra trùng lặp trong Workspace nếu giá trị này thay đổi)
    private String projectCode; 
    
    // Các thông tin mô tả và hình ảnh (Tùy chọn)
    private String description;
    private String goal;
    private String coverImageUrl;
    
    // Mức độ ưu tiên mới (Enum)
    private ProjectPriority priority; 
    
    // Các mốc thời gian (Tùy chọn)
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedAt;
    
    // ID tham chiếu (Tùy chọn)
    private Integer managerId; 
    private Integer projectTypeId; 
    
    // Cấu hình bảng (Lưu dưới dạng chuỗi JSON)
    private String boardConfig; 
}