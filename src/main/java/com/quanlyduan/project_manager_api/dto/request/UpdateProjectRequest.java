package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProjectRequest {
    @Size(min = 1, max = 255, message = "Tên dự án phải từ 1 đến 255 ký tự")
    private String name;
    private String projectCode; // validate unique within workspace if changed
    private String description;
    private String goal;
    private String coverImageUrl;
    private Priority priority; // cho phép cập nhật priority
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedAt;
    private Integer managerId; // optional
    private Integer projectTypeId; // optional
    private String boardConfig; // JSON string
}

