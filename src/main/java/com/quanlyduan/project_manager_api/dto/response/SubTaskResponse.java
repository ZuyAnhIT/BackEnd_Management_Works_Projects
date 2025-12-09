// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SubTaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một SubTask (Công việc phụ).
 * SubTask luôn phải gắn liền với một Task cha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTaskResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của SubTask
    private Integer id;
    private Integer projectId;
    // ID của Task cha (Công việc chính chứa SubTask này)
    private Integer parentTaskId;

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    // Tiêu đề công việc phụ
    private String title;

    // Mô tả chi tiết
    private String description;

    // Thứ tự sắp xếp trong danh sách SubTask của Task cha
    private Integer sortOrder;

    // ========================================================================
    // 3. TRẠNG THÁI & TIẾN ĐỘ (STATUS & PROGRESS)
    // ========================================================================

    // Trạng thái hiện tại (TO_DO, IN_PROGRESS, DONE)
    private SubTaskStatus status;

    // Thời gian ước tính để hoàn thành (đơn vị: giờ)
    private BigDecimal estimatedHours;

    // ========================================================================
    // 4. THÔNG TIN NHÂN SỰ (ASSIGNMENT)
    // ========================================================================

    // ID người được giao việc
    private Integer assigneeId;

    // Tên hiển thị người được giao việc
    private String assigneeName;

    // Đường dẫn Avatar người được giao việc
    private String assigneeAvatar;

    // ========================================================================
    // 5. THÔNG TIN HỆ THỐNG (AUDIT)
    // ========================================================================

    // ID người tạo SubTask
    private Integer createdById;

    // Tên người tạo
    private String createdByName;

    // Thời điểm tạo
    private LocalDateTime createdAt;

    // Thời điểm cập nhật lần cuối
    private LocalDateTime updatedAt;
}