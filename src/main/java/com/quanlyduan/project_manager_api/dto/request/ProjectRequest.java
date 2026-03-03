package com.quanlyduan.project_manager_api.dto.request;

// JSON Processing
import com.fasterxml.jackson.databind.JsonNode;

// Enums
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;

// Validation
import jakarta.validation.constraints.NotBlank;

// Java Utils
import java.time.LocalDate;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Dự án (Project).
 * Chứa các thông tin cơ bản, cấu hình hệ thống và kế hoạch triển khai ban đầu.
 */
@Data
public class ProjectRequest {

    // ==========================================
    // 1. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    /**
     * Tên dự án.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Project name must not be blank")
    private String name;

    /**
     * Mã định danh dự án (Ví dụ: "WEB", "APP", "CRM").
     * Mã này thường dùng để tạo tiền tố cho Task ID (VD: WEB-1, WEB-2).
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Project code must not be blank")
    private String projectCode;

    /**
     * Mô tả chi tiết về dự án.
     * (Tùy chọn)
     */
    private String description;

    /**
     * Mục tiêu chính cần đạt được của dự án.
     * (Tùy chọn)
     */
    private String goal;

    /**
     * Đường dẫn URL của ảnh bìa (Cover Image).
     * (Tùy chọn) Thường là link ảnh đã được upload qua một API riêng biệt.
     */
    private String coverImageUrl;

    // ==========================================
    // 2. CẤU HÌNH & THAM CHIẾU (CONFIG & REFERENCES)
    // ========================================================================

    /**
     * Cấu hình bảng công việc (Board Configuration).
     * Sử dụng JsonNode để nhận trực tiếp Object JSON (Dynamic structure) từ Client mà không cần parse String.
     * (Tùy chọn)
     */
    private JsonNode boardConfig; 

    /**
     * ID của Loại dự án (Ví dụ: Kanban, Scrum, Bug Tracking).
     * (Tùy chọn)
     */
    private Integer projectTypeId; 

    /**
     * ID của Người quản lý dự án (Project Manager).
     * (Tùy chọn)
     */
    private Integer managerId;     

    // ==========================================
    // 3. THÔNG TIN KẾ HOẠCH (PLANNING)
    // ========================================================================

    /**
     * Mức độ ưu tiên của dự án (LOW, MEDIUM, HIGH, URGENT).
     * (Tùy chọn) Nếu để null, tầng Service sẽ tự động gán giá trị mặc định (thường là MEDIUM).
     */
    private ProjectPriority priority;
    
    /**
     * Ngày bắt đầu dự kiến của dự án.
     * (Tùy chọn)
     */
    private LocalDate startDate;
    
    /**
     * Ngày kết thúc dự kiến (Deadline/Due Date) của dự án.
     * (Tùy chọn)
     */
    private LocalDate dueDate;

}