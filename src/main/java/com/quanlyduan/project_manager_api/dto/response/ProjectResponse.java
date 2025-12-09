// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returning Project information after creation or for retrieval operations.
 * Prevents direct Entity exposure (avoiding infinite recursion/lazy loading issues) 
 * while maintaining minimal necessary mapping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY & HIERARCHY)
    // ========================================================================

    // ID định danh của dự án
    private Integer id;

    // ID của Không gian làm việc chứa dự án này
    private Integer workspaceId;
    private Integer companyId;
    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    // Tên dự án
    private String name;

    // Mã định danh dự án (ví dụ: "WEB-01")
    private String projectCode;

    // Mô tả chi tiết
    private String description;

    // Mục tiêu của dự án
    private String goal;

    // Đường dẫn ảnh bìa
    private String coverImageUrl;

    // ========================================================================
    // 3. TRẠNG THÁI & TIẾN ĐỘ (STATUS & PROGRESS)
    // ========================================================================

    // Trạng thái hiện tại (Mapping từ Enum hoặc Object sang String để hiển thị)
    private String status;

    // Mức độ ưu tiên
    private String priority;

    // Tiến độ hoàn thành (tính theo %)
    private BigDecimal progress;

    // ========================================================================
    // 4. THÔNG TIN THỜI GIAN (TIMELINE)
    // ========================================================================

    // Ngày bắt đầu dự kiến
    private LocalDate startDate;

    // Hạn chót hoàn thành (Deadline)
    private LocalDate dueDate;

    // Ngày thực tế hoàn thành
    private LocalDate completedAt;

    // ========================================================================
    // 5. THÔNG TIN NHÂN SỰ (PEOPLE)
    // ========================================================================

    // ID người quản lý dự án (Project Manager)
    private Integer managerId;
    
    // Tên hiển thị của người quản lý
    private String managerName;

    // ID người tạo dự án
    private Integer createdById;

    // Tên hiển thị của người tạo
    private String createdByName;

    // ========================================================================
    // 6. THÔNG TIN HỆ THỐNG (AUDIT)
    // ========================================================================

    // Thời điểm tạo bản ghi
    private LocalDateTime createdAt;

    // Thời điểm cập nhật lần cuối
    private LocalDateTime updatedAt;
}