// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/SprintDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi thông tin chi tiết của một Sprint.
 * Bao gồm thông tin cơ bản, thời gian, các chỉ số thống kê và danh sách các công việc bên trong.
 * Thường được sử dụng ở màn hình Board hoặc báo cáo Sprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintDetailsResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của Sprint
    private Integer id;

    // ID của Dự án chứa Sprint này
    private Integer projectId;

    // ========================================================================
    // 2. THÔNG TIN CƠ BẢN (BASIC INFO)
    // ========================================================================

    // Tên Sprint (ví dụ: "Sprint 1 - Login Feature")
    private String name;

    // Mục tiêu của Sprint (Sprint Goal)
    private String goal;

    // Trạng thái hiện tại (NOT_STARTED, IN_PROGRESS, COMPLETED)
    private SprintStatus status;

    // ========================================================================
    // 3. THÔNG TIN THỜI GIAN (TIMELINE)
    // ========================================================================

    // Thời gian bắt đầu
    private LocalDateTime startDate;

    // Thời gian kết thúc dự kiến
    private LocalDateTime endDate;

    // ========================================================================
    // 4. CHỈ SỐ THỐNG KÊ (METRICS) - Dữ liệu tính toán
    // ========================================================================

    // Tổng số điểm câu chuyện (Story Points) của tất cả task trong Sprint.
    // Giúp PO đánh giá khối lượng công việc (Workload).
    private Long totalStoryPoints;

    // Tổng số lượng công việc (Task) có trong Sprint.
    private Integer taskCount;

    // ========================================================================
    // 5. DANH SÁCH CÔNG VIỆC (CONTENT)
    // ========================================================================

    // Danh sách tóm tắt các Task thuộc Sprint này.
    // Dùng để hiển thị lên bảng Kanban hoặc danh sách chi tiết.
    private List<TaskSummaryResponse> tasks; 
}