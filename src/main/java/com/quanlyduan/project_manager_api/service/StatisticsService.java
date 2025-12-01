// File: src/main/java/com/quanlyduan/project_manager_api/service/StatisticsService.java
package com.quanlyduan.project_manager_api.service;

import java.time.LocalDate;
import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.EpicProgressResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.RoadmapItemResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;

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

    /**
     * Lấy dữ liệu phân bổ mức độ ưu tiên (Urgent, High, Medium, Low).
     */
    List<PriorityDistributionResponse> getTaskPriorityDistribution(Integer projectId, Integer assigneeId);

    /**
     * Lấy dữ liệu phân bổ loại công việc (Story, Bug, Task...).
     */
    List<TaskTypeDistributionResponse> getTaskTypeDistribution(Integer projectId, Integer assigneeId);

    /**
     * Lấy dữ liệu phân bổ công việc (Workload) theo Stacked Bar Chart.
     * * @param projectId ID dự án
     * @param viewType "POINTS" hoặc "HOURS"
     * @param groupBy "STATUS" hoặc "PRIORITY"
     * @param ... các filter khác (sprintId, date range...)
     */
    List<WorkloadResponse> getWorkloadDistribution(
            Integer projectId, 
            String viewType, 
            String groupBy,
            Integer sprintId, LocalDate from, LocalDate to, List<Integer> statusIds
    );

    /**
     * Lấy danh sách tiến độ của các Epic trong dự án.
     * Tính toán dựa trên số lượng Task và Story Points đã hoàn thành so với tổng số.
     *
     * @param projectId  (Bắt buộc) ID của dự án.
     * @param sprintId   (Tùy chọn) Lọc task trong một Sprint cụ thể thuộc Epic.
     * @param from       (Tùy chọn) Lọc task bắt đầu từ ngày này.
     * @param to         (Tùy chọn) Lọc task kết thúc trước ngày này.
     * @param statusIds  (Tùy chọn) Chỉ tính các task thuộc các trạng thái này (ví dụ: chỉ tính task Active).
     * @return Danh sách các đối tượng chứa thông tin tiến độ Epic.
     */
    List<EpicProgressResponse> getEpicProgress(
            Integer projectId,
            Integer sprintId,
            LocalDate from,
            LocalDate to,
            List<Integer> statusIds
    );

    /**
     * Lấy dữ liệu cho biểu đồ Roadmap/Timeline.
     * @param projectId ID dự án.
     * @param viewType Loại dữ liệu muốn xem ("EPIC", "SPRINT", "ALL").
     * @return Danh sách các item để vẽ lên trục thời gian.
     */
    List<RoadmapItemResponse> getProjectRoadmap(Integer projectId, String viewType);
}