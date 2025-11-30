// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateSprintRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO nhận dữ liệu khi tạo mới một Sprint.
 * Hỗ trợ cơ chế "Tạo nhanh" (Quick Create), do đó tất cả các trường đều là tùy chọn (Optional).
 */
@Data
public class CreateSprintRequest {
    
    // Tên của Sprint (Tùy chọn).
    // Nếu null hoặc rỗng, hệ thống sẽ tự động sinh tên (ví dụ: "Sprint 1", "Sprint 2").
    private String name; 

    // Mục tiêu của Sprint (Tùy chọn).
    private String goal;
    
    // Thời gian bắt đầu dự kiến (Tùy chọn).
    // Định dạng: YYYY-MM-DDTHH:mm:ss
    private LocalDateTime startDate;
    
    // Thời gian kết thúc dự kiến (Tùy chọn).
    private LocalDateTime endDate;
    
    // Danh sách ID của các Task muốn thêm ngay vào Sprint này khi tạo (Tùy chọn).
    private List<Integer> taskIds; 
}