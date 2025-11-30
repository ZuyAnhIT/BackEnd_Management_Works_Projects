// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ReorderStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * DTO nhận dữ liệu cho hành động sắp xếp lại vị trí các cột trạng thái (Kéo thả cột).
 * Client sẽ gửi lên một danh sách chứa toàn bộ ID của các status trong dự án theo thứ tự mới mong muốn.
 */
@Data
public class ReorderStatusRequest {

    // Danh sách ID trạng thái đã được sắp xếp theo thứ tự mới (Bắt buộc)
    // Ví dụ: [3, 1, 2, 4] nghĩa là cột có ID=3 nằm đầu tiên, sau đó đến 1, 2, và 4.
    @NotEmpty(message = "Ordered status IDs list must not be empty")
    private List<Integer> orderedStatusIds; 
}