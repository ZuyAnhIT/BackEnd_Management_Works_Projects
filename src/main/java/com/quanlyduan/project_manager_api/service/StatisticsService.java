package com.quanlyduan.project_manager_api.service;

import java.time.LocalDate;
import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.CalendarEventResponse;
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
 * Service chuyen trach cung cap du lieu Thong ke, Bao cao va Bieu do (Analytics).
 * Xu ly cac logic tinh toan phan bo cong viec, tien do muc tieu (Epic) va lo trinh du an (Roadmap).
 */
public interface StatisticsService {

    // ======================================================
    // 1. THONG KE TONG QUAN (OVERVIEW STATISTICS)
    // ======================================================

    /**
     * Lay cac con so thong ke tong hop voi bo loc linh hoat.
     * Dung cho Dashboard du an hoan Dashboard ca nhan.
     * * @param projectId ID du an (Bat buoc khi xem Dashboard du an)
     * @param assigneeId ID nguoi dung (Bat buoc khi xem Dashboard ca nhan)
     * @return Doi tuong chua cac chi so thong ke (Count, Progress, v.v.)
     */
    StatisticsResponse getOverviewStatistics(
            Integer projectId, Integer assigneeId,
            LocalDate from, LocalDate to,
            String keyword, Integer filterAssigneeId,
            TaskPriority priority, TaskType taskType, List<Integer> statusIds
    );

    // ======================================================
    // 2. PHAN BO CONG VIEC (WORK DISTRIBUTION - PIE CHARTS)
    // ======================================================

    /**
     * Lay du lieu phan bo theo Trang thai (To Do, In Progress, Done).
     * Phuc vu ve bieu do tron (Pie Chart) hien thi ty le hoan thanh.
     */
    List<StatusDistributionResponse> getTaskStatusDistribution(Integer projectId, Integer assigneeId);

    /**
     * Lay du lieu phan bo theo Muc do uu tien (Urgent, High, Medium, Low).
     */
    List<PriorityDistributionResponse> getTaskPriorityDistribution(Integer projectId, Integer assigneeId);

    /**
     * Lay du lieu phan bo theo Loai cong viec (Story, Bug, Task).
     */
    List<TaskTypeDistributionResponse> getTaskTypeDistribution(Integer projectId, Integer assigneeId);

    // ======================================================
    // 3. NAN G LUC VA TIEN DO (CAPACITY & PROGRESS)
    // ======================================================

    /**
     * Lay du lieu tai cong viec (Workload) duoi dang Stacked Bar Chart.
     * * @param viewType Loai thong so ("POINTS" hoac "HOURS")
     * @param groupBy Tieu chi nhom du lieu ("STATUS" hoac "PRIORITY")
     */
    List<WorkloadResponse> getWorkloadDistribution(
            Integer projectId, 
            String viewType, 
            String groupBy,
            Integer sprintId, LocalDate from, LocalDate to, List<Integer> statusIds
    );

    /**
     * Lay thong tin tien do thuc te cua cac Epic (Muc tieu lon).
     * Tinh toan dua tren ty le hoan thanh Story Points hoac Task Count.
     */
    List<EpicProgressResponse> getEpicProgress(
            Integer projectId, Integer sprintId,
            LocalDate from, LocalDate to, List<Integer> statusIds
    );

    // ======================================================
    // 4. LO TRINH VA LICH TRINH (ROADMAP & CALENDAR)
    // ======================================================

    /**
     * Truy xuat du lieu cho bieu do lo trinh Roadmap (Gantt Chart).
     * Ket hop thoi gian cua ca Epic va Sprint de hien thi tren truc timeline.
     * * @param viewType Kieu hien thi ("EPIC", "SPRINT", hoac "ALL")
     */
    List<RoadmapItemResponse> getProjectRoadmap(
            Integer projectId, String viewType, 
            List<Integer> epicIds, List<EpicStatus> epicStatuses,
            List<Integer> sprintIds, List<SprintStatus> sprintStatuses,
            String keyword, LocalDate from, LocalDate to
    );

    /**
     * Lay du lieu su kien hien thi tren Lich du an (Calendar View).
     * * @param showSprints Co cho phep hien thi cac khoang thoi gian Sprint hay khong
     */
    List<CalendarEventResponse> getProjectCalendar(
            Integer projectId, LocalDate from, LocalDate to,
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType,
            boolean showSprints
    );

    // ======================================================
    // 5. XUAT BAO CAO (REPORT EXPORTING)
    // ======================================================

    /**
     * Xuat du lieu phan bo tai cong viec ra tep tin Excel.
     */
    byte[] exportWorkloadDistributionToExcel(
            List<WorkloadResponse> data, String viewType, String groupBy,
            Integer sprintId, LocalDate from, LocalDate to
    );

    /**
     * Xuat bao cao tien do Epic ra tep tin Excel.
     */
    byte[] exportEpicProgressToExcel(
            List<EpicProgressResponse> data, Integer projectId, 
            Integer sprintId, LocalDate from, LocalDate to
    );
}