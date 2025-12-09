// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/EpicServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.EpicSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.EpicService;

@Service
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public EpicServiceImpl(EpicRepository epicRepository,
                           TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           SecurityService securityService) {
        this.epicRepository = epicRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.securityService = securityService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH EPIC (LIST & SEARCH)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<EpicResponse> getEpicsByProject(Integer projectId, String keyword) {

        // 1. Áp dụng Specification (Lọc theo Project ID và keyword)
        Specification<Epic> spec = EpicSpecification.filterEpics(projectId, keyword);
        List<Epic> epics = epicRepository.findAll(spec);

        // 2. Chuyển đổi sang DTO và tính toán Metrics
        return epics.stream()
                .map(this::mapToEpicResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // 2. TẠO EPIC MỚI (CREATE EPIC)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "EPIC", description = "Create new Epic")
    public EpicResponse createEpic(Integer projectId, CreateEpicRequest request) {

        // Lấy thông tin người tạo hiện tại
        User creator = securityService.getCurrentAuthenticatedUser();

        // 1. Tìm Project
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // 2. KIỂM TRA TRÙNG TÊN: Tránh trùng lặp Epic name trong cùng một dự án
        if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Epic name '" + request.getName() + "' already exists in this project.");
        }

        // 3. Sinh Mã Epic (Dựa trên Project Code và Sequence tiếp theo)
        String projectCode = project.getProjectCode();
        long nextSequence = epicRepository.countByProject_Id(projectId) + 1;
        String epicCode = projectCode + "-E-" + nextSequence;

        // 4. Tạo Entity và thiết lập giá trị mặc định
        Epic newEpic = Epic.builder()
                .project(project)
                .name(request.getName())
                .description(request.getDescription())

                // Màu sắc được gửi lên (có thể null)
                .color(request.getColor())

                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .epicCode(epicCode)

                // MẶC ĐỊNH: OPEN
                .status(EpicStatus.OPEN)

                .createdBy(creator)
                .build();

        newEpic = epicRepository.save(newEpic);

        // 5. Map sang DTO và trả về
        return mapToEpicResponse(newEpic);
    }

    // ======================================================
    // 3. CẬP NHẬT EPIC (UPDATE EPIC)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "EPIC", description = "Update Epic")
    public EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request) {
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found."));

        if (!epic.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Mismatched project ID for this Epic.");
        }

        StringBuilder changes = new StringBuilder();

        // 1. Name
        if (request.getName() != null && !request.getName().trim().isEmpty() && !request.getName().equals(epic.getName())) {
             if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                 throw new BadRequestException("Epic name already exists.");
             }
             if (changes.length() > 0) changes.append(", ");
             changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", epic.getName(), request.getName()));
             epic.setName(request.getName());
        }

        // 2. Status
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                EpicStatus newStatus = EpicStatus.valueOf(request.getStatus().toUpperCase());
                if (newStatus != epic.getStatus()) {
                    if (changes.length() > 0) changes.append(", ");
                    changes.append(String.format("changed status from <strong>%s</strong> to <strong>%s</strong>", epic.getStatus(), newStatus));
                    epic.setStatus(newStatus);
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid Epic status: " + request.getStatus());
            }
        }

        // 3. Other fields (Color, Dates, Description) - Update without logging specific details
        if (request.getDescription() != null) epic.setDescription(request.getDescription());
        if (request.getColor() != null && !request.getColor().trim().isEmpty()) epic.setColor(request.getColor());
        if (request.getStartDate() != null) epic.setStartDate(request.getStartDate());
        if (request.getDueDate() != null) epic.setDueDate(request.getDueDate());

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
            //  ActivityLogContext.setDetail("updated details");
        }

        Epic savedEpic = epicRepository.save(epic);
        return mapToEpicResponse(savedEpic);
    }

    // ======================================================
    // 4. XÓA EPIC (DELETE EPIC)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "EPIC", description = "Delete Epic")
    public void deleteEpic(Integer projectId, Integer epicId) {
        // 1. Tìm Epic
        Epic epic = epicRepository.findById(epicId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found."));

        // 2. Validate: Epic phải thuộc về Project đang thao tác
        if (!epic.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Epic does not belong to this project.");
        }

        // 3. KIỂM TRA RÀNG BUỘC: Nếu Epic đang chứa Task -> Chặn xóa
        if (taskRepository.existsByEpic_Id(epicId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException(
                "Cannot delete this Epic because it contains tasks. " +
                "Please move or remove all tasks before deletion."
            );
        }

        // 4. Nếu không có ràng buộc -> Xóa
        epicRepository.delete(epic);
    }

    // ======================================================
    // 5. XEM CHI TIẾT EPIC (GET EPIC DETAILS)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public EpicResponse getEpicDetails(Integer projectId, Integer epicId) {
        // 1. Tìm Epic
        Epic epic = epicRepository.findById(epicId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found with ID: " + epicId));

        // 2. Validate: Epic phải thuộc Project
        if (!epic.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Epic does not belong to the specified project.");
        }

        // 3. Map và Trả về (Tái sử dụng hàm mapToEpicResponse để có cả metrics)
        return mapToEpicResponse(epic);
    }

    // ======================================================
    // ⚙️ PRIVATE UTILITY: MAPPER VÀ TÍNH TOÁN METRICS
    // ======================================================

    /**
     * Helper: Map Epic Entity sang EpicResponse DTO và tính toán Metrics liên quan.
     */
    private EpicResponse mapToEpicResponse(Epic epic) {

        // 1. Lấy danh sách Task thuộc Epic này (để tính toán)
        List<Task> tasksInEpic = taskRepository.findByEpicId(epic.getId());

        // 2. Tính toán Metrics
        Integer totalTasks = tasksInEpic.size();
        Integer tasksCompleted = (int) tasksInEpic.stream()
                // Giả định: Task được coi là hoàn thành nếu ProjectStatus có cờ isCompletedStatus = true
                .filter(task -> task.getStatus() != null &&
                                task.getStatus().getIsCompletedStatus() != null &&
                                task.getStatus().getIsCompletedStatus())
                .count();

        // 3. Tính toán % hoàn thành (Progress)
        double progress = 0.0;
        if (totalTasks > 0) {
            progress = (double) tasksCompleted * 100 / totalTasks;
        }

        // 4. Build và trả về DTO
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
                // Đảm bảo phần trăm không vượt quá 100
                .progressPercentage(Math.min(100.0, progress))
                .build();
    }
}