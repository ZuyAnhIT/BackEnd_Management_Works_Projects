// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/EpicServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest; 
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
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
    
    // --- API TẠO EPIC MỚI (ĐÃ SỬA THEO YÊU CẦU) ---
    @Override
    @Transactional
    public EpicResponse createEpic(Integer projectId, CreateEpicRequest request) {
        
        User creator = securityService.getCurrentAuthenticatedUser();

        // 1. Tìm Project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId)); // Đã dịch
        
        // 2. *** KIỂM TRA TRÙNG TÊN ***
        // (Nếu đã có Epic cùng tên trong dự án này thì báo lỗi)
        if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            throw new BadRequestException("Tên Epic '" + request.getName() + "' đã tồn tại trong dự án này."); // Đã dịch
        }

        // 3. Sinh Mã Epic
        String projectCode = project.getProjectCode(); 
        long nextSequence = epicRepository.countByProject_Id(projectId) + 1;
        String epicCode = projectCode + "-E-" + nextSequence; 
        
        // 4. Tạo Entity
        Epic newEpic = Epic.builder()
                .project(project) 
                .name(request.getName())
                .description(request.getDescription())
                
                // *** SỬA: Không random màu, null thì để null ***
                .color(request.getColor()) 
                
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .epicCode(epicCode) 
                
                // *** MẶC ĐỊNH: OPEN ***
                .status(EpicStatus.OPEN) 
                
                .createdBy(creator)
                .build();
        
        newEpic = epicRepository.save(newEpic);
        
        return mapToEpicResponse(newEpic);
    }

    // LOGIC CAP NHAT EPIC
    @Override
    @Transactional
    public EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Epic"));
        if (!epic.getProject().getId().equals(projectId)) throw new BadRequestException("Sai dự án");

        // (Có thể thêm check trùng tên ở đây nếu đổi tên)
        if (request.getName() != null && !request.getName().equals(epic.getName())) {
             if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                 throw new BadRequestException("Tên Epic đã tồn tại.");
             }
             epic.setName(request.getName());
        }

        // 3. Cập nhật thông tin (Chỉ cập nhật nếu có dữ liệu gửi lên)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            epic.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            epic.setDescription(request.getDescription());
        }
        
        if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
            epic.setColor(request.getColor());
        }
        
        if (request.getStartDate() != null) {
            epic.setStartDate(request.getStartDate());
        }
        
        if (request.getDueDate() != null) {
            epic.setDueDate(request.getDueDate());
        }
        
        // 4. Cập nhật Trạng thái (Xử lý Enum an toàn)
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                EpicStatus newStatus = EpicStatus.valueOf(request.getStatus().toUpperCase());
                epic.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái Epic không hợp lệ: " + request.getStatus()); // Đã dịch
            }
        }

        // 5. Lưu và trả về
        Epic savedEpic = epicRepository.save(epic);
        return mapToEpicResponse(savedEpic);
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
                .epicCode(epic.getEpicCode()) 
                .description(epic.getDescription()) 
                .color(epic.getColor())
                .status(epic.getStatus() != null ? epic.getStatus().name() : "OPEN")
                .projectId(epic.getProject().getId())
                .startDate(epic.getStartDate()) 
                .dueDate(epic.getDueDate())     
                .createdAt(epic.getCreatedAt())
                .totalTasks(totalTasks)
                .tasksCompleted(tasksCompleted)
                .progressPercentage(Math.min(100.0, progress)) 
                .build();
    }
}