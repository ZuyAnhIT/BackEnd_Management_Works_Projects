// File: com.quanlyduan.project_manager_api.service.impl.EpicServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.EpicSpecification;
import com.quanlyduan.project_manager_api.service.EpicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpicServiceImpl implements EpicService {
    
    private final EpicRepository epicRepository;
    private final TaskRepository taskRepository;
    // ... (inject các repository khác)

    public EpicServiceImpl(EpicRepository epicRepository, TaskRepository taskRepository) {
        this.epicRepository = epicRepository;
        this.taskRepository = taskRepository;
    }

    // --- TRIỂN KHAI HÀM LẤY DANH SÁCH EPIC ---
    @Override
    @Transactional(readOnly = true)
    public List<EpicResponse> getEpicsByProject(Integer projectId, String keyword) {
        
        // 1. Áp dụng Specification (Lọc theo Project ID và keyword)
        Specification<Epic> spec = EpicSpecification.filterEpics(projectId, keyword);
        List<Epic> epics = epicRepository.findAll(spec);
        
        // 2. Chuyển đổi và tính toán Metrics
        return epics.stream()
                .map(this::mapToEpicResponse)
                .collect(Collectors.toList());
    }
    
    // --- PRIVATE UTILITY: MAPPER VÀ LOGIC TÍNH TOÁN METRICS ---
    private EpicResponse mapToEpicResponse(Epic epic) {
        
        // Lấy danh sách Task thuộc Epic này
        List<Task> tasksInEpic = taskRepository.findByEpicId(epic.getId());

        // Tính toán Metrics
        Integer totalTasks = tasksInEpic.size();
        Integer tasksCompleted = (int) tasksInEpic.stream()
                // Giả định: Task được coi là hoàn thành nếu ProjectStatus có cờ isCompletedStatus = true
                .filter(task -> task.getStatus() != null && 
                                 task.getStatus().getIsCompletedStatus() != null && 
                                 task.getStatus().getIsCompletedStatus())
                .count();

        // Tính toán %
        double progress = 0.0;
        if (totalTasks > 0) {
            progress = (double) tasksCompleted * 100 / totalTasks;
        }

        return EpicResponse.builder()
                .id(epic.getId())
                .name(epic.getName())
                .color(epic.getColor())
                .projectId(epic.getProject().getId())
                .createdAt(epic.getCreatedAt())
                .totalTasks(totalTasks)
                .tasksCompleted(tasksCompleted)
                .progressPercentage(Math.min(100.0, progress)) // Đảm bảo không vượt quá 100%
                .build();
    }
}