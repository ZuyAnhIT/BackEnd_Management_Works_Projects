// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ReorderStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ReorderStatusRequest {
    @NotEmpty(message = "Danh sách ID trạng thái không được để trống") // Đã dịch
    private List<Integer> orderedStatusIds; 
    // Ví dụ gửi lên: [3, 1, 2, 4] (ID theo thứ tự từ trái sang phải)
}