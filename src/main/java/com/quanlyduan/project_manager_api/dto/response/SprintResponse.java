// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SprintResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi thông tin cơ bản của một Sprint.
 * Thường được sử dụng trong các thao tác CRUD Sprint (Tạo, Cập nhật, Bắt đầu...).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH
    // ========================================================================

    // ID định danh của Sprint
    private Integer id;

    // ID của Dự án chứa Sprint này
    private Integer projectId;

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN
    // ========================================================================

    // Tên Sprint (ví dụ: "Sprint 1")
    private String name;

    // Mục tiêu của Sprint (Sprint Goal)
    private String goal;

    // Trạng thái hiện tại (NOT_STARTED, IN_PROGRESS, COMPLETED)
    private String status;

    // ========================================================================
    // 3. THÔNG TIN THỜI GIAN
    // ========================================================================

    // Thời gian bắt đầu thực tế/dự kiến
    private LocalDateTime startDate;

    // Thời gian kết thúc dự kiến
    private LocalDateTime endDate;

    // ========================================================================
    // 4. DANH SÁCH CÔNG VIỆC
    // ========================================================================

    // Danh sách các Task thuộc Sprint này.
    // Lưu ý: Nếu chỉ cần danh sách tóm tắt, cân nhắc sử dụng TaskSummaryResponse trong tương lai.
    private List<TaskResponse> tasks;
}