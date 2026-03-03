package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Trạng thái dự án.
 * Đại diện cho một cột (Column) trên bảng công việc (Board).
 */
@Data
public class CreateProjectStatusRequest {

    // ==========================================
    // REQUEST DATA (Thông tin trạng thái)
    // ==========================================

    /**
     * Tên trạng thái (Ví dụ: "To Do", "In Progress", "Done").
     * Bắt buộc phải nhập, tối đa 100 ký tự.
     */
    @NotBlank(message = "Status name must not be blank")
    @Size(max = 100, message = "Status name must not exceed 100 characters")
    private String name;

    /**
     * Mã màu HEX đại diện cho trạng thái để hiển thị trên UI.
     * Ví dụ: #3498db (Xanh), #e74c3c (Đỏ).
     * (Tùy chọn)
     */
    private String color;

    /**
     * Cờ đánh dấu xác định đây có phải là trạng thái "Hoàn thành" hay không.
     * - true: Task nằm ở trạng thái này được hệ thống coi là đã hoàn tất.
     * - false: Task vẫn đang trong quá trình thực hiện hoặc chờ xử lý.
     */
    private Boolean isCompletedStatus;

}