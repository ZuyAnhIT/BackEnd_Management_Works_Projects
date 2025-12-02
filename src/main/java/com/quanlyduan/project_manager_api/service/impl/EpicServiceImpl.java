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
    public EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request) {
        // 1. Tìm Epic
        Epic epic = epicRepository.findById(epicId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found."));

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo Epic thuộc đúng Project
        if (!epic.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Mismatched project ID for this Epic.");
        }

        // 3. Cập nhật Tên (kèm kiểm tra trùng tên nếu đổi tên)
        if (request.getName() != null && !request.getName().trim().isEmpty() && !request.getName().equals(epic.getName())) {
             // Nếu tên mới khác tên cũ, kiểm tra trùng lặp
             if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                 // Sửa thông báo sang tiếng Anh
                 throw new BadRequestException("Epic name already exists.");
             }
             epic.setName(request.getName());
        }

        // (Logic trùng lặp: Nếu dòng trên đã xử lý tên, dòng dưới chỉ là redundant check cho trường hợp tên là null/empty, nhưng ta giữ nguyên code gốc)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
             // Mặc dù đã xử lý tên ở trên, nhưng giữ lại logic này để bảo toàn code gốc nếu tên có thể được set lại (redundant)
             // epic.setName(request.getName());
        }


        // 4. Cập nhật các trường thông tin khác (Chỉ cập nhật nếu có dữ liệu gửi lên)
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

        // 5. Cập nhật Trạng thái (Xử lý Enum an toàn)
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                EpicStatus newStatus = EpicStatus.valueOf(request.getStatus().toUpperCase());
                epic.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Invalid Epic status: " + request.getStatus());
            }
        }

        // 6. Lưu và trả về
        Epic savedEpic = epicRepository.save(epic);
        return mapToEpicResponse(savedEpic);
    }

    // ======================================================
    // 4. XÓA EPIC (DELETE EPIC)
    // ======================================================
    @Override
    @Transactional
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