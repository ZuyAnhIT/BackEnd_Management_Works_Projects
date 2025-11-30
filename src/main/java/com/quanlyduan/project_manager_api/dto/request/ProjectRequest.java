// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ProjectRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu khi tạo mới một Dự án (Project).
 * Chứa các thông tin cơ bản, cấu hình và kế hoạch ban đầu.
 */
@Data
public class ProjectRequest {

    // ========================================================================
    // 1. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    // Tên dự án (Bắt buộc, không được để trống)
    @NotBlank(message = "Project name must not be blank")
    private String name;

    // Mã định danh dự án (Bắt buộc - ví dụ: "WEB", "APP")
    @NotBlank(message = "Project code must not be blank")
    private String projectCode;

    // Mô tả chi tiết về dự án (Tùy chọn)
    private String description;

    // Mục tiêu chính của dự án (Tùy chọn)
    private String goal;

    // Đường dẫn ảnh bìa (Tùy chọn - thường là link ảnh hoặc xử lý qua multipart riêng)
    private String coverImageUrl;

    // ========================================================================
    // 2. CẤU HÌNH & THAM CHIẾU (CONFIG & REFERENCES)
    // ========================================================================

    // Cấu hình bảng công việc (Board Configuration).
    // Sử dụng JsonNode để nhận trực tiếp Object JSON từ Client mà không cần parse String.
    private JsonNode boardConfig; 

    // ID của Loại dự án (Tùy chọn)
    private Integer projectTypeId; 

    // ID của Người quản lý dự án (Tùy chọn)
    private Integer managerId;     

    // ========================================================================
    // 3. THÔNG TIN KẾ HOẠCH (PLANNING)
    // ========================================================================

    // Mức độ ưu tiên của dự án (Enum: LOW, MEDIUM, HIGH, URGENT).
    // Nếu null, Service sẽ gán mặc định (thường là MEDIUM).
    private ProjectPriority priority;
    
    // Ngày bắt đầu dự kiến
    private LocalDate startDate;
    
    // Ngày kết thúc dự kiến (Deadline)
    private LocalDate dueDate;
}