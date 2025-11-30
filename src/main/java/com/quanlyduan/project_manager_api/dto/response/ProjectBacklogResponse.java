// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectBacklogResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO phản hồi dữ liệu tổng hợp cho màn hình Backlog (Scrum Board).
 * Cấu trúc màn hình này thường chia làm 2 phần chính:
 * 1. Các Sprint đang chạy/sắp chạy (Active Sprints) - thường ở phía trên.
 * 2. Danh sách công việc tồn đọng (Product Backlog) - thường ở phía dưới.
 */
@Data
@Builder
public class ProjectBacklogResponse {
    
    // ========================================================================
    // PHẦN 1: ACTIVE SPRINTS
    // ========================================================================

    // Danh sách các Sprint đang ở trạng thái IN_PROGRESS hoặc NOT_STARTED.
    // Mỗi Sprint trong list này đã bao gồm danh sách Task con của nó.
    // Thường phần này KHÔNG phân trang (load hết các sprint active).
    private List<SprintDetailsResponse> activeSprints;
    
    // ========================================================================
    // PHẦN 2: PRODUCT BACKLOG
    // ========================================================================

    // Danh sách các Task chưa được gán vào bất kỳ Sprint nào (sprint_id IS NULL).
    // Phần này CÓ phân trang (Infinite Scroll hoặc Load More).
    private List<TaskSummaryResponse> backlogTasks;

    // ========================================================================
    // PHẦN 3: METADATA PHÂN TRANG (CHO BACKLOG)
    // ========================================================================

    // Số trang hiện tại của danh sách Backlog.
    private int backlogPageNumber;

    // Kích thước trang (số lượng task backlog trả về lần này).
    private int backlogPageSize;

    // Tổng số lượng task đang nằm trong Backlog.
    private long backlogTotalElements;

    // Tổng số trang của Backlog.
    private int backlogTotalPages;
}