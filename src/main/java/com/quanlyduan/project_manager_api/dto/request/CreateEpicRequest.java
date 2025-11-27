// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateEpicRequest {
    
    @NotBlank(message = "Tên Epic không được để trống")
    @Size(max = 255, message = "Tên Epic không được quá 255 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;
    
    private String color; 
    
    private LocalDate startDate;
    private LocalDate dueDate;

}