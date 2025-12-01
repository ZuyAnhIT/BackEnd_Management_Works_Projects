// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/StatisticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.service.StatisticsService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quanlyduan.project_manager_api.model.ProjectStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TaskRepository taskRepository;
    private final ProjectServiceImpl projectService; // Để dùng mapper

    public StatisticsServiceImpl(TaskRepository taskRepository, ProjectServiceImpl projectService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    // 1. Thống kê tuần (Weekly Statistics)
    @Transactional(readOnly = true)
    public StatisticsResponse getWeeklyStatistics(Integer projectId, Integer assigneeId) {
        
        // 1. Cấu hình thời gian
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        LocalDateTime dueSoonLimit = endDate.plusDays(3); // Sắp hết hạn trong 3 ngày tới

        // 2. Giới hạn số lượng items trả về trong list (Ví dụ: Top 10)
        // Để tránh payload quá nặng
        Pageable limit = PageRequest.of(0, 10);

        // 3. --- TRUY VẤN DỮ LIỆU ---

        // A. Task Sắp đến hạn (Due Soon)
        List<Task> dueTasks = taskRepository.findTasksDueSoon(projectId, assigneeId, endDate, dueSoonLimit);
        long dueCount = dueTasks.size(); // Hoặc count riêng nếu list bị limit

        // B. Task Đã tạo (Created)
        long createdCount = taskRepository.countCreatedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> createdTasks = taskRepository.findCreatedTasks(projectId, assigneeId, startDate, endDate, limit);

        // C. Task Đã hoàn thành (Completed)
        long completedCount = taskRepository.countCompletedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> completedTasks = taskRepository.findCompletedTasks(projectId, assigneeId, startDate, endDate, limit);

        // D. Task Đã cập nhật (Updated)
        long updatedCount = taskRepository.countUpdatedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> updatedTasks = taskRepository.findUpdatedTasks(projectId, assigneeId, startDate, endDate, limit);

        // 4. --- MAPPING DTO ---
        // Sử dụng hàm helper để code gọn hơn
        
        return StatisticsResponse.builder()
                .fromDate(startDate.toString())
                .toDate(endDate.toString())
                
                .dueSoonCount(dueCount)
                .dueSoonTasks(mapList(dueTasks))
                
                .createdCount(createdCount)
                .createdTasks(mapList(createdTasks))
                
                .completedCount(completedCount)
                .completedTasks(mapList(completedTasks))
                
                .updatedCount(updatedCount)
                .updatedTasks(mapList(updatedTasks))
                .build();
    }


    // 2. Dữ liệu phân bổ trạng thái (Pie Chart Data)
    @Transactional(readOnly = true)
    public List<StatusDistributionResponse> getTaskStatusDistribution(Integer projectId, Integer assigneeId) {
        
        // 1. Gọi Repo lấy dữ liệu thô (Group By)
        List<Object[]> results = taskRepository.countTasksByStatusGroup(projectId, assigneeId);

        // 2. Tính tổng số task để tính phần trăm
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        // 3. Map sang DTO
        List<StatusDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            ProjectStatus status = (ProjectStatus) row[0]; // Phần tử 0 là Entity Status
            Long count = (Long) row[1];                    // Phần tử 1 là Count

            // Xử lý trường hợp status bị null (nếu có task chưa gán status)
            if (status == null) {
                responseList.add(StatusDistributionResponse.builder()
                        .statusId(null)
                        .statusName("Unassigned") // Hoặc "No Status"
                        .color("#95a5a6") // Màu xám
                        .taskCount(count)
                        .percentage(calculatePercentage(count, totalTasks))
                        .build());
                continue;
            }

            responseList.add(StatusDistributionResponse.builder()
                    .statusId(status.getId())
                    .statusName(status.getName())
                    .color(status.getColor())
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }


    // HEPER METHODS

    // Helper tính phần trăm làm tròn 2 chữ số
    private Double calculatePercentage(Long count, Long total) {
        if (total == 0) return 0.0;
        return Math.round((double) count / total * 10000.0) / 100.0;
    }

    // Helper: Map List<Entity> -> List<DTO>
    private List<TaskSummaryResponse> mapList(List<Task> tasks) {
        return tasks.stream()
                .map(projectService::mapToTaskSummaryResponse) // Cần đảm bảo hàm này là PUBLIC trong ProjectService
                .collect(Collectors.toList());
    }
}