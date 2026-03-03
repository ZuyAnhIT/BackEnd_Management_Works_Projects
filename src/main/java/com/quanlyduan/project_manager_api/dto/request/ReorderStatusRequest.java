package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotEmpty;

// Java Utils
import java.util.List;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho hành động sắp xếp lại vị trí của các cột trạng thái (Status Columns).
 * Thường được sử dụng khi người dùng thực hiện thao tác kéo thả toàn bộ cột trên bảng công việc (Board).
 */
@Data
public class ReorderStatusRequest {

    // ==========================================
    // REQUEST DATA (Thông tin sắp xếp cột)
    // ==========================================

    /**
     * Danh sách ID của các trạng thái đã được sắp xếp theo thứ tự mới mong muốn.
     * Ví dụ: Gửi lên [10, 5, 8] nghĩa là cột có ID=10 sẽ nằm vị trí đầu tiên (Index 0), sau đó đến 5 và 8.
     * Bắt buộc phải có ít nhất một ID trong danh sách.
     */
    @NotEmpty(message = "Ordered status IDs list must not be empty")
    private List<Integer> orderedStatusIds; 

}