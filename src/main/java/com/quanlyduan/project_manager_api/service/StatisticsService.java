// File: src/main/java/com/quanlyduan/project_manager_api/service/StatisticsService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Thống kê và Báo cáo.
 */
public interface StatisticsService {

    /**
     * Lấy thống kê hoạt động trong 7 ngày gần nhất.
     * * @param projectId  (Tùy chọn) Nếu có, chỉ thống kê trong dự án này.
     * @param assigneeId (Tùy chọn) Nếu có, chỉ thống kê công việc của người này.
     * @return Đối tượng chứa số liệu thống kê và danh sách task sắp đến hạn.
     */
    StatisticsResponse getWeeklyStatistics(Integer projectId, Integer assigneeId);

    /**
     * Lấy dữ liệu phân bổ trạng thái (To Do, In Progress, Done...) để vẽ biểu đồ tròn (Pie Chart).
     * Hàm này tính toán số lượng task và phần trăm tỷ lệ của từng trạng thái.
     *
     * @param projectId  (Tùy chọn) ID dự án.
     * @param assigneeId (Tùy chọn) ID người được giao (để xem biểu đồ cá nhân).
     * @return Danh sách các đối tượng chứa thông tin trạng thái, số lượng và phần trăm.
     */
    List<StatusDistributionResponse> getTaskStatusDistribution(Integer projectId, Integer assigneeId);
}