// File: src/main/java/com/quanlyduan/project_manager_api/service/StatisticsService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;

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
}