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

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    public static final String ENTITY_EPIC = "EPIC";

    public static final String DESC_CREATE_EPIC = "Create new Epic";
    public static final String DESC_UPDATE_EPIC = "Update Epic";
    public static final String DESC_DELETE_EPIC = "Delete Epic";

    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found with ID: ";
    public static final String ERROR_EPIC_NAME_EXISTS = "Epic name already exists in this project.";
    public static final String ERROR_EPIC_NOT_FOUND = "Epic not found.";
    public static final String ERROR_EPIC_NOT_FOUND_ID = "Epic not found with ID: ";
    public static final String ERROR_MISMATCHED_PROJECT = "Mismatched project ID for this Epic.";
    public static final String ERROR_INVALID_EPIC_STATUS = "Invalid Epic status: ";
    public static final String ERROR_EPIC_CONTAINS_TASKS = "Cannot delete this Epic because it contains tasks. Please move or remove all tasks before deletion.";
    public static final String ERROR_EPIC_WRONG_PROJECT = "Epic does not belong to the specified project.";

    public static final String EPIC_CODE_PREFIX = "-E-";
    public static final String DEFAULT_STATUS_OPEN = "OPEN";

    public static final String LOG_RENAMED_MSG = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_STATUS_CHANGED_MSG = "changed status from <strong>%s</strong> to <strong>%s</strong>";

    public static final double MAX_PROGRESS_PERCENTAGE = 100.0;

    // Khai bao cac bien phu thuoc
    private final EpicRepository epicRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public EpicServiceImpl(EpicRepository epicRepository,
                           TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           SecurityService securityService) {
        this.epicRepository = epicRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.securityService = securityService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public List<EpicResponse> getEpicsByProject(Integer projectId, String keyword) {
        // Ap dung dieu kien loc theo du an va tu khoa
        Specification<Epic> spec = EpicSpecification.filterEpics(projectId, keyword);
        List<Epic> epics = epicRepository.findAll(spec);

        // Chuyen doi danh sach Entity sang DTO va tinh toan tien do
        return epics.stream()
                .map(this::mapToEpicResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_EPIC, description = DESC_CREATE_EPIC)
    public EpicResponse createEpic(Integer projectId, CreateEpicRequest request) {
        // Lay thong tin nguoi dung hien tai
        User creator = securityService.getCurrentAuthenticatedUser();

        // Tim kiem du an
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND + projectId));

        // Kiem tra trung lap ten Epic trong cung du an
        if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            throw new BadRequestException(ERROR_EPIC_NAME_EXISTS);
        }

        // Sinh ma Epic tu dong dua tren ma du an
        String projectCode = project.getProjectCode();
        long nextSequence = epicRepository.countByProject_Id(projectId) + 1;
        String epicCode = projectCode + EPIC_CODE_PREFIX + nextSequence;

        // Khoi tao Epic moi voi cac gia tri mac dinh
        Epic newEpic = Epic.builder()
                .project(project)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .epicCode(epicCode)
                .status(EpicStatus.OPEN)
                .createdBy(creator)
                .build();

        newEpic = epicRepository.save(newEpic);

        return mapToEpicResponse(newEpic);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_EPIC, description = DESC_UPDATE_EPIC)
    public EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request) {
        // Tim kiem Epic can cap nhat
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND));

        // Xac thuc quyen so huu cua du an voi Epic
        if (!epic.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_MISMATCHED_PROJECT);
        }

        StringBuilder changes = new StringBuilder();

        // Cap nhat ten Epic neu co su thay doi
        if (request.getName() != null && !request.getName().trim().isEmpty() && !request.getName().equals(epic.getName())) {
             if (epicRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                 throw new BadRequestException(ERROR_EPIC_NAME_EXISTS);
             }
             if (changes.length() > 0) {
                 changes.append(", ");
             }
             changes.append(String.format(LOG_RENAMED_MSG, epic.getName(), request.getName()));
             epic.setName(request.getName());
        }

        // Cap nhat trang thai Epic
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                EpicStatus newStatus = EpicStatus.valueOf(request.getStatus().toUpperCase());
                if (newStatus != epic.getStatus()) {
                    if (changes.length() > 0) {
                        changes.append(", ");
                    }
                    changes.append(String.format(LOG_STATUS_CHANGED_MSG, epic.getStatus(), newStatus));
                    epic.setStatus(newStatus);
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ERROR_INVALID_EPIC_STATUS + request.getStatus());
            }
        }

        // Cap nhat cac truong thong tin khac ma khong can ghi log chi tiet
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

        // Ghi log vao context neu co thay doi
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        Epic savedEpic = epicRepository.save(epic);
        return mapToEpicResponse(savedEpic);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_EPIC, description = DESC_DELETE_EPIC)
    public void deleteEpic(Integer projectId, Integer epicId) {
        // Tim kiem Epic can xoa
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND));

        // Xac thuc quyen so huu cua du an voi Epic
        if (!epic.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_EPIC_WRONG_PROJECT);
        }

        // Kiem tra rang buoc neu Epic dang chua cong viec thi khong cho phep xoa
        if (taskRepository.existsByEpic_Id(epicId)) {
            throw new BadRequestException(ERROR_EPIC_CONTAINS_TASKS);
        }

        epicRepository.delete(epic);
    }

    @Override
    @Transactional(readOnly = true)
    public EpicResponse getEpicDetails(Integer projectId, Integer epicId) {
        // Tim kiem chi tiet Epic hien tai
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND_ID + epicId));

        // Xac thuc quyen so huu
        if (!epic.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_EPIC_WRONG_PROJECT);
        }

        return mapToEpicResponse(epic);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private EpicResponse mapToEpicResponse(Epic epic) {
        // Lay danh sach cac cong viec thuoc Epic de phuc vu viec tinh toan
        List<Task> tasksInEpic = taskRepository.findByEpicId(epic.getId());

        // Tinh toan cac chi so thong ke
        Integer totalTasks = tasksInEpic.size();
        Integer tasksCompleted = (int) tasksInEpic.stream()
                .filter(task -> task.getStatus() != null &&
                                task.getStatus().getIsCompletedStatus() != null &&
                                task.getStatus().getIsCompletedStatus())
                .count();

        // Tinh toan phan tram hoan thanh
        double progress = 0.0;
        if (totalTasks > 0) {
            progress = (double) tasksCompleted * 100 / totalTasks;
        }

        // Chuyen doi sang DTO va tra ve ket qua
        return EpicResponse.builder()
                .id(epic.getId())
                .name(epic.getName())
                .epicCode(epic.getEpicCode())
                .description(epic.getDescription())
                .color(epic.getColor())
                .status(epic.getStatus() != null ? epic.getStatus().name() : DEFAULT_STATUS_OPEN)
                .projectId(epic.getProject().getId())
                .startDate(epic.getStartDate())
                .dueDate(epic.getDueDate())
                .createdAt(epic.getCreatedAt())
                .totalTasks(totalTasks)
                .tasksCompleted(tasksCompleted)
                .progressPercentage(Math.min(MAX_PROGRESS_PERCENTAGE, progress))
                .build();
    }
}