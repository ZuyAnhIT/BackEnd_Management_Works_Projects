// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin tóm tắt của một Không gian làm việc (Workspace).
 * Dùng cho danh sách (list view) hoặc khi xem chi tiết Workspace.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {

    // ========================================================================
    // 1. ĐỊNH DANH & CẤP BẬC (IDENTITY & HIERARCHY)
    // ========================================================================

    // ID định danh của Workspace (Primary Key)
    private Integer workspaceId;

    // ID của Công ty chứa Workspace này (Context cha)
    private Integer companyId;

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN & TRỰC QUAN (BASIC INFO & VISUALS)
    // ========================================================================

    // Tên hiển thị của Workspace
    private String workspaceName;

    // Mô tả chi tiết
    private String description;
    
    // Đường dẫn ảnh bìa (Cover Image URL)
    private String coverImage;

    // Mã màu đại diện (ví dụ: #3498db)
    private String color;

    // ========================================================================
    // 3. TRẠNG THÁI & HỆ THỐNG (STATE & AUDIT)
    // ========================================================================

    // ID của người đã tạo Workspace này
    private Integer createdById;

    // Trạng thái hiện tại của Workspace (ACTIVE, ARCHIVED, DELETED)
    private String status;

    // Thời điểm tạo bản ghi
    private LocalDateTime createdAt;
}