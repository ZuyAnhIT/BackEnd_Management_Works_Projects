// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateProjectStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Trạng thái dự án (Cột trên Board).
 */
@Data
public class CreateProjectStatusRequest {

    // Tên trạng thái (Bắt buộc, tối đa 100 ký tự)
    // Ví dụ: "To Do", "In Progress", "Done"
    @NotBlank(message = "Status name must not be blank")
    @Size(max = 100, message = "Status name must not exceed 100 characters")
    private String name;

    // Mã màu HEX đại diện cho trạng thái (Tùy chọn)
    // Ví dụ: #3498db (Xanh), #e74c3c (Đỏ)
    private String color;

    // Cờ đánh dấu: Đây có phải là trạng thái "Hoàn thành" không?
    // true: Task ở trạng thái này được coi là đã xong.
    // false: Task vẫn đang thực hiện hoặc chờ.
    private Boolean isCompletedStatus;
}