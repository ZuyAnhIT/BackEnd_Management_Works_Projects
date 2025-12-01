// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/StatisticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl {

    private final TaskRepository taskRepository;
    private final ProjectServiceImpl projectService; // Để dùng mapper

    public StatisticsServiceImpl(TaskRepository taskRepository, ProjectServiceImpl projectService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

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

    // Helper: Map List<Entity> -> List<DTO>
    private List<TaskSummaryResponse> mapList(List<Task> tasks) {
        return tasks.stream()
                .map(projectService::mapToTaskSummaryResponse) // Cần đảm bảo hàm này là PUBLIC trong ProjectService
                .collect(Collectors.toList());
    }
}