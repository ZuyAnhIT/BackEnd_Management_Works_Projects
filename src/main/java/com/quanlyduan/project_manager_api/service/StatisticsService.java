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
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Thống kê và Báo cáo.
 */
public interface StatisticsService {

    /**
     * Lấy thống kê tổng quan (Overview) với bộ lọc linh hoạt.
     * * @param projectId        ID dự án (Bắt buộc nếu xem Project Dashboard).
     * @param assigneeId       ID người dùng (Nếu xem Personal Dashboard, hệ thống tự truyền).
     * @param from             Ngày bắt đầu thống kê.
     * @param to               Ngày kết thúc thống kê.
     * @param keyword          Từ khóa tìm kiếm task.
     * @param filterAssigneeId ID người dùng muốn lọc (khi xem Project Dashboard).
     * @param priority         Độ ưu tiên.
     * @param taskType         Loại task.
     * @param statusIds        Danh sách ID trạng thái.
     * @return Đối tượng chứa các con số thống kê và danh sách chi tiết.
     */
    StatisticsResponse getOverviewStatistics(
            Integer projectId, Integer assigneeId,
            LocalDate from, LocalDate to,
            String keyword, Integer filterAssigneeId,
            TaskPriority priority, TaskType taskType, List<Integer> statusIds
    );

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
     * Lấy dữ liệu cho biểu đồ Roadmap/Timeline (Gantt Chart).
     * Trả về danh sách các Epic và Sprint đã được chuẩn hóa để vẽ lên trục thời gian.
     *
     * @param projectId    (Bắt buộc) ID dự án.
     * @param viewType     Loại dữ liệu hiển thị: "EPIC", "SPRINT", hoặc "ALL".
     * @param epicIds      (Tùy chọn) Lọc theo danh sách ID Epic cụ thể.
     * @param epicStatuses (Tùy chọn) Lọc Epic theo trạng thái (ví dụ: chỉ xem OPEN, IN_PROGRESS).
     * @param keyword      (Tùy chọn) Tìm kiếm Epic theo tên hoặc mã.
     * @param sprintIds    (Tùy chọn) Lọc theo danh sách ID Sprint cụ thể.
     * @param sprintStatuses (Tùy chọn) Lọc Sprint theo trạng thái (ví dụ: chỉ xem IN_PROGRESS, COMPLETED).
     * @param from         (Tùy chọn) Ngày bắt đầu của khung nhìn timeline (Start View).
     * @param to           (Tùy chọn) Ngày kết thúc của khung nhìn timeline (End View).
     * @return Danh sách các item (Epic/Sprint) có thông tin ngày tháng và tiến độ.
     */
    List<RoadmapItemResponse> getProjectRoadmap(
            Integer projectId,
            String viewType,        // EPIC, SPRINT, ALL
            
            List<Integer> epicIds,
            List<EpicStatus> epicStatuses,
            
            List<Integer> sprintIds,
            List<SprintStatus> sprintStatuses,
            
            String keyword,         // Tìm chung
            LocalDate from,         // View Start
            LocalDate to            // View End
    );
}