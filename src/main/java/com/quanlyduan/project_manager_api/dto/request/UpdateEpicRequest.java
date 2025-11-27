// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateEpicRequest {
    
    @Size(max = 255, message = "Tên Epic không được quá 255 ký tự") // Đã dịch
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự") // Đã dịch
    private String description;
    
    private String color; // (Ví dụ: #8e44ad)
    
    private String status; // (OPEN, IN_PROGRESS, COMPLETED, CLOSED)
    
    private LocalDate startDate;
    private LocalDate dueDate;
}