 // File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectStatusResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin cấu hình của một Trạng thái (Cột) trong Dự án.
 * Được sử dụng để vẽ giao diện Board (Kanban/Scrum) và hiển thị danh sách trạng thái trong cài đặt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của trạng thái (Primary Key).
    // Dùng để thực hiện các thao tác Update, Delete hoặc Move Task.
    private Integer id;

    // ID của dự án chứa trạng thái này.
    private Integer projectId;
    private Integer workspaceId; 
    private Integer companyId;

    // ========================================================================
    // 2. THÔNG TIN HIỂN THỊ (DISPLAY INFO)
    // ========================================================================

    // Tên hiển thị của trạng thái (ví dụ: "To Do", "In Progress", "Done").
    private String name;

    // Mã màu HEX dùng để tô màu cho cột hoặc nhãn trạng thái (ví dụ: "#3498db").
    private String color;

    // ========================================================================
    // 3. THÔNG TIN CẤU HÌNH (CONFIG INFO)
    // ========================================================================

    // Số thứ tự sắp xếp của cột trên giao diện Board (0, 1, 2...).
    // Cột có số nhỏ hơn sẽ nằm bên trái.
    private Integer sortOrder;

    // Cờ đánh dấu: Đây có phải là trạng thái "Hoàn thành" hay không?
    // true: Task nằm ở cột này được tính là đã xong (Progress 100%).
    // false: Task vẫn đang trong quy trình xử lý.
    private Boolean isCompletedStatus;
}