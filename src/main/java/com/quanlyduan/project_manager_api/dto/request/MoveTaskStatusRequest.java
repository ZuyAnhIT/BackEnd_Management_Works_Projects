package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotNull;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho hành động di chuyển Task sang một cột trạng thái khác.
 * Thường được sử dụng khi người dùng thực hiện thao tác kéo thả (Drag & Drop) trên bảng công việc (Board).
 */
@Data
public class MoveTaskStatusRequest {

    // ==========================================
    // REQUEST DATA (Thông tin di chuyển)
    // ==========================================

    /**
     * ID của trạng thái (cột) mới mà Task sẽ được chuyển đến.
     * Bắt buộc phải có, không được để null.
     */
    @NotNull(message = "New status ID must not be null")
    private Integer newStatusId;

    /**
     * Vị trí sắp xếp (Index) mong muốn của Task sau khi thả vào cột mới (Ví dụ: 0, 1, 2...).
     * (Tùy chọn) Nếu để null, hệ thống sẽ mặc định đẩy Task này xuống vị trí cuối cùng của cột.
     */
    private Integer newSortOrder;

}