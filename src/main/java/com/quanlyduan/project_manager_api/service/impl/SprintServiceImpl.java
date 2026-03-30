package com.quanlyduan.project_manager_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SprintService;
import com.quanlyduan.project_manager_api.service.TaskService;

@Service
public class SprintServiceImpl implements SprintService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_START = "START";
    public static final String ACTION_COMPLETE = "COMPLETE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    public static final String ENTITY_SPRINT = "SPRINT";

    public static final String DESC_CREATE_SPRINT = "Create a new Sprint";
    public static final String DESC_START_SPRINT = "Start Sprint";
    public static final String DESC_COMPLETE_SPRINT = "Complete Sprint";
    public static final String DESC_UPDATE_SPRINT = "Update Sprint";
    public static final String DESC_DELETE_SPRINT = "Delete Sprint";

    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found.";
    public static final String ERROR_USER_CREATOR_NOT_FOUND = "User creator not found.";
    public static final String ERROR_SPRINT_NOT_FOUND = "Sprint not found.";
    public static final String ERROR_SPRINT_NOT_FOUND_ID = "Sprint not found with ID: ";
    public static final String ERROR_SPRINT_WRONG_PROJECT = "Sprint does not belong to this project.";
    public static final String ERROR_SPRINT_ALREADY_STARTED = "Sprint has already been started or completed.";
    public static final String ERROR_SPRINT_NOT_IN_PROGRESS = "Only in-progress Sprints can be completed.";
    public static final String ERROR_INVALID_DATE_RANGE = "Start date cannot be after end date.";
    public static final String ERROR_DELETE_COMPLETED_SPRINT = "Cannot delete a completed Sprint.";
    public static final String ERROR_INVALID_STATUS = "Invalid status: ";
    public static final String ERROR_SPRINT_WRONG_PROJECT_DETAILS = "Sprint not found in the specified project.";

    public static final String SPRINT_NAME_PREFIX = "Sprint ";
    public static final String LOG_RENAMED = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_GOAL_UPDATED = "updated goal";

    // Khai bao cac bien phu thuoc
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final TaskService taskService;

    // Constructor khoi tao thu cong
    public SprintServiceImpl(SprintRepository sprintRepository,
                             ProjectRepository projectRepository,
                             TaskRepository taskRepository,
                             UserRepository userRepository,
                             SecurityService securityService,
                             TaskService taskService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.taskService = taskService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_SPRINT, description = DESC_CREATE_SPRINT)
    public SprintResponse createSprint(Integer projectId, CreateSprintRequest request) {
        Integer currentUserId = securityService.getCurrentUserId();

        // Kiem tra ton tai cua du an va nguoi dung
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_CREATOR_NOT_FOUND));

        // Logic sinh ten tu dong neu nguoi dung khong nhap ten
        String sprintName = request.getName();
        if (sprintName == null || sprintName.trim().isEmpty()) {
            long count = sprintRepository.countByProject_Id(projectId);
            sprintName = SPRINT_NAME_PREFIX + (count + 1);
        }

        // Khoi tao chu ky phat trien (Sprint) moi
        Sprint sprint = Sprint.builder()
                .project(project)
                .name(sprintName)
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.NOT_STARTED)
                .createdBy(creator)
                .build();

        Sprint savedSprint = sprintRepository.save(sprint);

        // Chuyen cac cong viec tu danh sach cho (Backlog) vao Sprint vua tao
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            List<Task> tasksToUpdate = taskRepository.findAllById(request.getTaskIds());
            for (Task task : tasksToUpdate) {
                task.setSprint(savedSprint);
            }
            taskRepository.saveAll(tasksToUpdate);
        }

        // Tra ve ket qua thong qua doi tuong DTO, khong kem danh sach cong viec
        return mapToSprintResponse(savedSprint, Collections.emptyList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_START, entityType = ENTITY_SPRINT, description = DESC_START_SPRINT)
    public SprintResponse startSprint(Integer projectId, Integer sprintId) {
        // Tim kiem Sprint theo ID
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));

        // Xac thuc Sprint nay thuoc dung du an dang yeu cau
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_SPRINT_WRONG_PROJECT);
        }
        
        // Chi cho phep bat dau nhung Sprint chua dien ra
        if (sprint.getStatus() != SprintStatus.NOT_STARTED) {
            throw new BadRequestException(ERROR_SPRINT_ALREADY_STARTED);
        }
        
        // Cap nhat trang thai va thoi gian bat dau neu chua duoc thiet lap
        sprint.setStatus(SprintStatus.IN_PROGRESS);
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDateTime.now());
        }

        Sprint savedSprint = sprintRepository.save(sprint);
        
        // Lay toan bo cac cong viec nam trong Sprint de tra ve
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_COMPLETE, entityType = ENTITY_SPRINT, description = DESC_COMPLETE_SPRINT)
    public SprintResponse completeSprint(Integer projectId, Integer sprintId) {
        // Tim kiem Sprint theo ID
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));

        // Xac thuc Sprint thuoc ve du an va phai dang trong trang thai dien ra
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_SPRINT_WRONG_PROJECT);
        }
        
        if (sprint.getStatus() != SprintStatus.IN_PROGRESS) {
            throw new BadRequestException(ERROR_SPRINT_NOT_IN_PROGRESS);
        }

        // Don dep cac cong viec chua hoan thanh trong Sprint de tra ve Backlog
        List<Task> incompleteTasks = taskRepository.findIncompleteTasksBySprintId(sprintId);

        if (!incompleteTasks.isEmpty()) {
            for (Task task : incompleteTasks) {
                task.setSprint(null);
            }
            taskRepository.saveAll(incompleteTasks);
        }

        // Chot thoi gian va cap nhat trang thai hoan thanh cho Sprint
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setEndDate(LocalDateTime.now());

        Sprint savedSprint = sprintRepository.save(sprint);

        // Lay danh sach cac cong viec da hoan thanh de tra ve cho Client
        List<Task> completedTasksOnly = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, completedTasksOnly);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_SPRINT, description = DESC_UPDATE_SPRINT)
    public SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request) {
        // Tim kiem Sprint theo ID
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));

        // Xac thuc quyen so huu cua du an
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_SPRINT_WRONG_PROJECT);
        }

        StringBuilder changes = new StringBuilder();

        // Cap nhat ten
        if (request.getName() != null && !request.getName().trim().isEmpty() && !request.getName().equals(sprint.getName())) {
             if (changes.length() > 0) {
                 changes.append(", ");
             }
             changes.append(String.format(LOG_RENAMED, sprint.getName(), request.getName()));
             sprint.setName(request.getName());
        }

        // Cap nhat muc tieu
        if (request.getGoal() != null && !request.getGoal().equals(sprint.getGoal())) {
             if (changes.length() > 0) {
                 changes.append(", ");
             }
             changes.append(LOG_GOAL_UPDATED);
             sprint.setGoal(request.getGoal());
        }

        // Cap nhat va kiem tra thoi gian bat dau - ket thuc
        LocalDateTime newStartDate = (request.getStartDate() != null) ? request.getStartDate() : sprint.getStartDate();
        LocalDateTime newEndDate = (request.getEndDate() != null) ? request.getEndDate() : sprint.getEndDate();

        if (newStartDate != null && newEndDate != null && newStartDate.isAfter(newEndDate)) {
            throw new BadRequestException(ERROR_INVALID_DATE_RANGE);
        }

        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        // Luu lich su thay doi vao context neu co
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        Sprint savedSprint = sprintRepository.save(sprint);
        return mapToSprintResponse(savedSprint, Collections.emptyList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_SPRINT, description = DESC_DELETE_SPRINT)
    public void deleteSprint(Integer projectId, Integer sprintId) {
        // Tim kiem Sprint theo ID
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));

        // Xac thuc quyen so huu cua du an
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_SPRINT_WRONG_PROJECT);
        }

        // Khong the xoa Sprint da hoan thanh
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException(ERROR_DELETE_COMPLETED_SPRINT);
        }

        long taskCount = taskRepository.countBySprint_Id(sprintId);

        // Kich ban xoa cung: Sprint chua bat dau va chua co cong viec nao duoc gan
        if (sprint.getStatus() == SprintStatus.NOT_STARTED && taskCount == 0) {
            sprintRepository.delete(sprint);
            return;
        }

        // Kich ban xoa mem (Huy Sprint): Day cong viec tro lai Backlog roi cap nhat trang thai
        if (taskCount > 0) {
            taskRepository.moveTasksToBacklogBySprintId(sprintId);
        }

        sprint.setStatus(SprintStatus.CANCELLED);
        sprint.setEndDate(LocalDateTime.now());
        
        sprintRepository.save(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(Integer projectId, String status) {
        List<Sprint> sprints;

        // Xy ly loc danh sach theo trang thai neu co truyen vao
        if (status != null && !status.trim().isEmpty()) {
            try {
                SprintStatus statusEnum = SprintStatus.valueOf(status.toUpperCase());
                sprints = sprintRepository.findAll().stream()
                        .filter(s -> s.getProject().getId().equals(projectId) && s.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ERROR_INVALID_STATUS + status);
            }
        } else {
            sprints = sprintRepository.findAll().stream()
                    .filter(s -> s.getProject().getId().equals(projectId))
                    .collect(Collectors.toList());
        }

        // Chuyen doi sang DTO va tra ve ket qua
        return sprints.stream()
                .map(sprint -> mapToSprintResponse(sprint, Collections.emptyList()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId) {
        // Tim kiem Sprint theo ID
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND_ID + sprintId));

        // Xac thuc quyen so huu cua du an
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException(ERROR_SPRINT_WRONG_PROJECT_DETAILS);
        }

        // Lay thong tin cong viec va chuyen sang DTO
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(sprintId);
        List<TaskSummaryResponse> taskDTOs = tasks.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // Tinh toan thong ke so luong va tong diem cong viec
        long totalPoints = tasks.stream()
                .mapToLong(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        int count = tasks.size();

        // Dong goi va tra ve phan hoi
        return SprintDetailsResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .tasks(taskDTOs)
                .totalStoryPoints(totalPoints)
                .taskCount(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getProjectIdBySprint(Integer sprintId) {
        // Tim kiem ID cua du an chua Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND_ID + sprintId));
        return sprint.getProject().getId();
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private SprintResponse mapToSprintResponse(Sprint sprint, List<Task> tasks) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus().name())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .build();
    }

    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus();

        // Tinh toan thong ke cong viec con
        int totalSubtasks = 0;
        int completedSubtasks = 0;
        if (task.getSubTasks() != null) {
            totalSubtasks = task.getSubTasks().size();
            completedSubtasks = (int) task.getSubTasks().stream()
                    .filter(st -> st.getStatus() == SubTaskStatus.DONE)
                    .count();
        }

        // Xu ly the phan loai
        List<TaskSummaryResponse.TagInfo> tagInfos = new ArrayList<>();
        if (task.getTags() != null) {
            tagInfos = task.getTags().stream()
                    .map(tag -> TaskSummaryResponse.TagInfo.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build())
                    .collect(Collectors.toList());
        }

        // Dong goi doi tuong cong viec tong quan
        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .projectId(task.getProject().getId())
                .workspaceId(task.getProject().getWorkspace().getId())
                .companyId(task.getProject().getWorkspace().getCompany().getId())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
                .status(status != null ? TaskSummaryResponse.StatusInfo.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .color(status.getColor())
                        .build() : null)
                .epic(epic != null ? TaskSummaryResponse.EpicInfo.builder()
                        .id(epic.getId())
                        .name(epic.getName())
                        .color(epic.getColor())
                        .build() : null)
                .assignee(assignee != null ? TaskSummaryResponse.UserInfo.builder()
                        .id(assignee.getId())
                        .name(assignee.getFullName())
                        .avatarUrl(assignee.getAvatarUrl())
                        .build() : null)
                .tags(tagInfos)
                .subtaskSummary(TaskSummaryResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())
                .build();
    }
}