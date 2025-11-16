// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateSprintRequest.java
// (MỚI) DTO cho US-S3-6: Tạo Sprint mới
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSprintRequest {
    // @NotNull(message = "Project ID is required")
    // private Integer projectId;

    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;

    // US-S3-6: Thêm task từ backlog vào sprint ngay khi tạo
    private List<Integer> taskIds;
}
