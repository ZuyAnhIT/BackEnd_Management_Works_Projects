// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin của một Trạng thái (Cột trên Board).
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 */
@Data
public class UpdateStatusRequest {
    
    // Tên trạng thái mới (Tùy chọn, tối đa 100 ký tự)
    @Size(max = 100, message = "Status name must not exceed 100 characters")
    private String name;

    // Mã màu hiển thị mới (Tùy chọn - ví dụ: #e74c3c)
    private String color; 

    // Cờ đánh dấu: Đây có phải là trạng thái "Hoàn thành" không? (Tùy chọn)
    // Nếu true: Các Task khi được kéo vào cột này sẽ được hệ thống ghi nhận là đã hoàn thành (Completed).
    private Boolean isCompletedStatus; 
}