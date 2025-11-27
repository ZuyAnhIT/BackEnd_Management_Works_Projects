// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/EpicServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest; // Đã sửa import
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.EpicSpecification;
import com.quanlyduan.project_manager_api.service.EpicService;
import com.quanlyduan.project_manager_api.security.SecurityService; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpicServiceImpl implements EpicService {
    
    private final EpicRepository epicRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SecurityService securityService; // Thêm SecurityService để lấy user creator

    // *** CONSTRUCTOR THỦ CÔNG ***
    public EpicServiceImpl(EpicRepository epicRepository, 
                           TaskRepository taskRepository, 
                           ProjectRepository projectRepository,
                           SecurityService securityService) {
        this.epicRepository = epicRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.securityService = securityService;
    }

    // --- 1. API LẤY DANH SÁCH EPIC (KÈM TÌM KIẾM & METRICS) ---
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
    
    // --- 2. API TẠO EPIC MỚI ---
    @Override
    @Transactional
    public EpicResponse createEpic(Integer projectId, CreateEpicRequest request) { // Sửa tham số
        
        // 0. Lấy người tạo
        var creator = securityService.getCurrentAuthenticatedUser();

        // 1. Tìm Project (Sửa lỗi constructor Exception)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId)); // Đã dịch
        
        // 2. Sinh Mã Epic (Ví dụ: QLD-E-1, QLD-E-2)
        String projectCode = project.getProjectCode(); 
        
        // Đếm số lượng Epic hiện có của dự án để lấy số thứ tự tiếp theo
        long nextSequence = epicRepository.countByProject_Id(projectId) + 1;
        String epicCode = projectCode + "-E-" + nextSequence; // Tạo mã theo format
        
        // 3. Tạo Entity
        Epic newEpic = Epic.builder()
                .project(project) 
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .epicCode(epicCode) 
                .createdBy(creator) // Gán người tạo
                .build();
        
        // 4. Lưu và trả về DTO
        newEpic = epicRepository.save(newEpic);
        
        return mapToEpicResponse(newEpic);
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
                .epicCode(epic.getEpicCode()) // Thêm trường này vào DTO
                .description(epic.getDescription()) // Thêm trường này vào DTO
                .color(epic.getColor())
                .projectId(epic.getProject().getId())
                .startDate(epic.getStartDate()) // Thêm
                .dueDate(epic.getDueDate())     // Thêm
                .createdAt(epic.getCreatedAt())
                .totalTasks(totalTasks)
                .tasksCompleted(tasksCompleted)
                .progressPercentage(Math.min(100.0, progress)) 
                .build();
    }
}