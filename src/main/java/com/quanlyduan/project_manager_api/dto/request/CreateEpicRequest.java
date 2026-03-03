package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Java Utils
import java.time.LocalDate;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Epic.
 */
@Data
public class CreateEpicRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Epic)
    // ==========================================

    /**
     * Tên của Epic.
     * Bắt buộc phải có, không vượt quá 255 ký tự.
     */
    @NotBlank(message = "Epic name must not be blank")
    @Size(max = 255, message = "Epic name must not exceed 255 characters")
    private String name;

    /**
     * Mô tả chi tiết về nội dung hoặc mục tiêu của Epic.
     * (Tùy chọn, không vượt quá 1000 ký tự)
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Mã màu HEX đại diện cho Epic trên giao diện (Ví dụ: #FF5733).
     * (Tùy chọn) Nếu để null, hệ thống sẽ tự động chọn một màu ngẫu nhiên.
     */
    private String color;

    /**
     * Ngày bắt đầu dự kiến của Epic.
     * (Tùy chọn)
     */
    private LocalDate startDate;

    /**
     * Ngày kết thúc dự kiến (Due Date) của Epic.
     * (Tùy chọn)
     */
    private LocalDate dueDate;

}