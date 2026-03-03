package com.quanlyduan.project_manager_api.dto.request;

// Swagger / OpenAPI
import io.swagger.v3.oas.annotations.media.Schema;

// Validation
import jakarta.validation.constraints.NotBlank;

// Java Utils
import java.math.BigDecimal;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một SubTask (Công việc con).
 * Tích hợp sẵn cấu hình Swagger để hiển thị tài liệu và ví dụ JSON mẫu trên giao diện API Docs.
 */
@Data
public class CreateSubTaskRequest {

    // ==========================================
    // REQUEST DATA (Thông tin SubTask)
    // ==========================================

    /**
     * Tiêu đề của SubTask.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Subtask title must not be blank")
    @Schema(description = "Title of the subtask", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * Mô tả chi tiết nội dung công việc.
     * (Tùy chọn)
     */
    @Schema(description = "Detailed description of the subtask")
    private String description;

    /**
     * ID của người được phân công thực hiện (Assignee).
     * (Tùy chọn) Có thể gửi giá trị null nếu SubTask này chưa được giao cho ai.
     */
    @Schema(description = "ID of the user assigned to this subtask (Optional)")
    private Integer assigneeId;

    /**
     * Thời gian ước tính để hoàn thành công việc.
     * Đơn vị tính: Giờ (Hours).
     * (Tùy chọn) Sử dụng BigDecimal để hỗ trợ độ chính xác cao khi nhập giờ lẻ (VD: 1.5 giờ).
     */
    @Schema(description = "Estimated hours to complete the subtask")
    private BigDecimal estimatedHours;

}