// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TagResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Thẻ (Tag) trong dự án.
 * Thẻ được sử dụng để phân loại Task (ví dụ: "Backend", "Frontend", "Urgent").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của Tag
    private Integer id;

    // ID của Dự án chứa Tag này
    private Integer projectId;

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    // Tên hiển thị của Tag (ví dụ: "Bug")
    private String name;

    // Mã màu hiển thị (Hex code)
    private String color;

    // Mô tả ý nghĩa của Tag
    private String description;

    // ========================================================================
    // 3. THÔNG TIN HỆ THỐNG (AUDIT INFO)
    // ========================================================================

    // ID người tạo Tag
    private Integer createdById;

    // Tên hiển thị người tạo
    private String createdByName;

    // Đường dẫn Avatar người tạo (để hiển thị tooltip hoặc icon nhỏ)
    private String createdByAvatar;

    // Thời điểm tạo
    private LocalDateTime createdAt;

    // Thời điểm cập nhật lần cuối
    private LocalDateTime updatedAt;
}