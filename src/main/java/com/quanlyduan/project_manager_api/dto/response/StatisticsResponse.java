// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/StatisticsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StatisticsResponse {
    // --- Metadata ---
    private String fromDate;
    private String toDate;

    // --- 1. Sắp đến hạn (Quan trọng nhất) ---
    private long dueSoonCount;
    private List<TaskSummaryResponse> dueSoonTasks; // Danh sách chi tiết

    // --- 2. Đã tạo mới ---
    private long createdCount;
    private List<TaskSummaryResponse> createdTasks; // Danh sách chi tiết (vd: Top 5 mới nhất)

    // --- 3. Đã hoàn thành ---
    private long completedCount;
    private List<TaskSummaryResponse> completedTasks; // Danh sách chi tiết

    // --- 4. Đã cập nhật ---
    private long updatedCount;
    private List<TaskSummaryResponse> updatedTasks; // Danh sách chi tiết
}