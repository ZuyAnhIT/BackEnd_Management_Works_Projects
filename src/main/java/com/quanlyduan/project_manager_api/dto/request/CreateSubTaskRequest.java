// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateSubTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu khi tạo mới một SubTask (Công việc phụ).
 * Class này tích hợp sẵn cấu hình Swagger để hiển thị ví dụ JSON mẫu trên giao diện tài liệu API.
 */
@Data
public class CreateSubTaskRequest {

    // Tiêu đề SubTask (Bắt buộc, không được để trống)
    @NotBlank(message = "Subtask title must not be blank")
    @Schema(description = "Title of the subtask", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    // Mô tả chi tiết công việc (Tùy chọn)
    @Schema(description = "Detailed description of the subtask")
    private String description;

    // ID người được giao việc (Tùy chọn - gửi null nếu chưa giao cho ai)
    @Schema(description = "ID of the user assigned to this subtask (Optional)")
    private Integer assigneeId;

    // Thời gian ước tính để hoàn thành (Tùy chọn, đơn vị giờ)
    @Schema(description = "Estimated hours to complete the subtask")
    private BigDecimal estimatedHours;
}