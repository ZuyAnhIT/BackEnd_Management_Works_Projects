// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    
    @Size(max = 100, message = "Tên trạng thái không được quá 100 ký tự") // Đã dịch
    private String name;

    private String color; // (Ví dụ: #e74c3c)

    private Boolean isCompletedStatus; // Đánh dấu cột này là "Hoàn thành"?
}