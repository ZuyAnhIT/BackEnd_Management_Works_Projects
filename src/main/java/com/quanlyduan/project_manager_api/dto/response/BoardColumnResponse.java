// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/BoardColumnResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO phản hồi cấu trúc của một Cột (Column/Status) trên bảng Kanban/Scrum.
 * Chứa thông tin về trạng thái và danh sách các Task đang nằm trong trạng thái đó.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {

    // ID định danh của trạng thái (ProjectStatus ID)
    private Integer statusId;

    // Tên hiển thị của cột (ví dụ: "To Do", "In Progress")
    private String statusName;

    // Mã màu đại diện cho cột (ví dụ: "#3498db")
    private String color;

    // Thứ tự hiển thị của cột trên giao diện (từ trái qua phải)
    private Integer order; 
    
    // Cờ đánh dấu: Đây có phải là cột "Hoàn thành" (Done) không?
    // Nếu true: Các task nằm ở đây được coi là đã xong.
    private Boolean isCompleted;

    // Danh sách chi tiết các công việc (Task) thuộc cột này
    // (Đã được lọc theo các tiêu chí tìm kiếm nếu có)
    private List<TaskResponse> tasks; 
}