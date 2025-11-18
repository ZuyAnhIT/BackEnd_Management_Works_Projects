// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateProjectStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectStatusRequest {

    @NotBlank(message = "Tên trạng thái không được để trống") // Đã dịch
    @Size(max = 100, message = "Tên trạng thái không được quá 100 ký tự") // Đã dịch
    private String name;

    private String color; // (Ví dụ: #3498db)

    private Boolean isCompletedStatus; // (true/false)
}