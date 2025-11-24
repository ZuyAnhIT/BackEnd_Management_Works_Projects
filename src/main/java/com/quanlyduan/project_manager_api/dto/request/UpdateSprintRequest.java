// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateSprintRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateSprintRequest {

    @Size(min = 1, max = 255, message = "Tên Sprint phải từ 1 đến 255 ký tự")
    private String name;

    private String goal;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}