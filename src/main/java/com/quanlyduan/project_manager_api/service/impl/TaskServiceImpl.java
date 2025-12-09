// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final EpicRepository epicRepository;
    private final ProjectStatusRepository projectStatusRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           SprintRepository sprintRepository,
                           UserRepository userRepository,
                           EpicRepository epicRepository,
                           SecurityService securityService,
                           ProjectStatusRepository projectStatusRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.epicRepository = epicRepository;
        this.projectStatusRepository = projectStatusRepository;
    }

    // ======================================================
    // 1. KÉO THẢ TASK (SPRINT + VỊ TRÍ)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "TASK", description = "Drag and drop Task with Sprint")
    public void updateTaskSprint(Integer taskId, Integer newSprintId, Integer newSortOrder) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        Integer projectId = task.getProject().getId();

        // 2. Xác định Sprint đích (hoặc Backlog)
        Sprint targetSprint = null;
        if (newSprintId != null) {
            targetSprint = sprintRepository.findById(newSprintId)
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));
            // Validate: Task và Sprint phải cùng Project
            if (!targetSprint.getProject().getId().equals(projectId)) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Task and Sprint must belong to the same project.");
            }
        }

        // 3. Xử lý Vị trí (SortOrder)
        if (newSortOrder == null) {
            // Nếu không gửi vị trí -> Mặc định xuống cuối cùng
            if (targetSprint != null) {
                // Cuối Sprint
                newSortOrder = taskRepository.findMaxSortOrderBySprintId(newSprintId) + 1;
            } else {
                // Cuối Backlog
                newSortOrder = taskRepository.findMaxSortOrderByProjectIdAndSprintIsNull(projectId) + 1;
            }
        } else {
            // Nếu có vị trí cụ thể -> Phải đẩy các task đang đứng đó lùi xuống
            if (targetSprint != null) {
                // Chuyển vị trí trong Sprint
                taskRepository.shiftSortOrderInSprint(newSprintId, newSortOrder);
            } else {
                // Chuyển vị trí trong Backlog
                taskRepository.shiftSortOrderInBacklog(projectId, newSortOrder);
            }
        }

        // 4. Cập nhật Task
        task.setSprint(targetSprint);
        task.setSortOrder(newSortOrder);

        taskRepository.save(task);
    }

    // ======================================================
    // 2. TẠO TASK MỚI (CREATE TASK)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "TASK", description = "Create new Task")
    public TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request) {

        // 1. Lấy thông tin người tạo
        User creator = securityService.getCurrentAuthenticatedUser();

        // 2. Lấy dự án
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        // 3. TỰ ĐỘNG TÌM TRẠNG THÁI (CỘT) MẶC ĐỊNH (Vị trí đầu tiên)
        ProjectStatus defaultStatus = projectStatusRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("This project has no status board configuration."));

        // 4. XỬ LÝ SPRINT (Backlog hoặc Sprint cụ thể)
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + request.getSprintId()));

            // Validate: Sprint phải thuộc Project này
            if (!sprint.getProject().getId().equals(projectId)) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Sprint does not belong to this project.");
            }
        }

        // 5. Xử lý các trường tùy chọn (Epic, Assignee)
        Epic epic = null;
        if (request.getEpicId() != null) {
            epic = epicRepository.findById(request.getEpicId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
        }
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found"));
        }

        // 6. SINH MÃ TASK TỰ ĐỘNG (Ví dụ: WEB-1, WEB-2)
        long taskCount = taskRepository.countByProjectId(projectId);
        String newCode = project.getProjectCode() + "-" + (taskCount + 1);

        // 7. Tạo Task Entity (Gán mặc định nếu thiếu)
        Task newTask = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskCode(newCode)

                // Mặc định là TASK nếu không chọn
                .taskType(request.getTaskType() != null ? request.getTaskType() : TaskType.TASK)

                // Mặc định vào cột đầu tiên
                .status(defaultStatus)

                // Mặc định là MEDIUM nếu không chọn
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)

                .sprint(sprint) // Null (Backlog) hoặc Object (Sprint)
                .epic(epic)
                .assignee(assignee)
                .assigner(creator)
                .createdBy(creator)
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .sortOrder((int) taskCount) // Mặc định xếp cuối cùng
                .build();

        Task savedTask = taskRepository.save(newTask);

        // 8. Map sang DTO và trả về
        return mapToTaskSummaryResponse(savedTask);
    }

    // ======================================================
    // 3. DI CHUYỂN TASK (STATUS + VỊ TRÍ TRÊN BOARD)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "MOVE_STATUS", entityType = "TASK", description = "Change task status")
    public void moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Integer projectId = task.getProject().getId();
        Integer newStatusId = request.getNewStatusId();

        // 2. Tìm Status mới
        ProjectStatus newStatus = projectStatusRepository.findById(newStatusId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + newStatusId));

        // 3. Validate Project
        if (!newStatus.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("New status does not belong to the task's project.");
        }

        // 4. Xử lý Vị Trí (Sort Order)
        Integer newSortOrder = request.getNewSortOrder();

        if (newSortOrder == null) {
            // Mặc định xuống cuối cột mới
            newSortOrder = taskRepository.findMaxSortOrderByStatusId(projectId, newStatusId) + 1;
        } else {
            // Nếu có vị trí cụ thể -> Phải đẩy các task đang đứng đó lùi xuống
            // Logic này sẽ đẩy các task khác có sortOrder >= newSortOrder trong cột MỚI lùi xuống
            taskRepository.shiftSortOrderInStatus(projectId, newStatusId, newSortOrder);
        }

        // 5. Cập nhật Task
        task.setStatus(newStatus);
        task.setSortOrder(newSortOrder);

        // 6. Cập nhật cờ hoàn thành (completedAt)
        if (newStatus.getIsCompletedStatus()) {
            task.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        taskRepository.save(task);
    }

    // ======================================================
    // 4. CẬP NHẬT TASK (UPDATE ALL FIELDS)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "TASK", description = "Update task information")
    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Integer projectId = task.getProject().getId();

        // 2. Cập nhật các trường Scalar (Văn bản/Số)
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        // 3. Cập nhật các Quan hệ (Cần validate)

        // A. Status (Cột)
        if (request.getStatusId() != null) {
            ProjectStatus newStatus = projectStatusRepository.findById(request.getStatusId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            if (!newStatus.getProject().getId().equals(projectId)) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Status does not belong to this project.");
            }
            task.setStatus(newStatus);
        }

        // B. Sprint
        if (request.getSprintId() != null) {
            if (request.getSprintId() == 0) {
                task.setSprint(null); // Gỡ bỏ sprint (về backlog)
            } else {
                Sprint sprint = sprintRepository.findById(request.getSprintId())
                        // Sửa thông báo sang tiếng Anh
                        .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
                if (!sprint.getProject().getId().equals(projectId)) {
                    // Sửa thông báo sang tiếng Anh
                    throw new BadRequestException("Sprint does not belong to this project.");
                }
                task.setSprint(sprint);
            }
        }

        // C. Epic
        if (request.getEpicId() != null) {
            if (request.getEpicId() == 0) {
                task.setEpic(null); // Gỡ bỏ Epic
            } else {
                Epic epic = epicRepository.findById(request.getEpicId())
                        // Sửa thông báo sang tiếng Anh
                        .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
                if (!epic.getProject().getId().equals(projectId)) {
                    // Sửa thông báo sang tiếng Anh
                    throw new BadRequestException("Epic does not belong to this project.");
                }
                task.setEpic(epic);
            }
        }

        // D. Assignee (Người được giao)
        if (request.getAssigneeId() != null) {
            if (request.getAssigneeId() == 0) {
                task.setAssignee(null); // Bỏ giao việc
            } else {
                User assignee = userRepository.findById(request.getAssigneeId())
                        // Sửa thông báo sang tiếng Anh
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                // (Nên check xem user có trong Project không, nhưng giữ nguyên logic cũ)
                task.setAssignee(assignee);
            }
        }

        // 4. Lưu và trả về
        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    // ======================================================
    // 5. GÁN/GỠ EPIC VÀO TASK
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "TASK", description = "Assign/Remove Epic to Task")
    public TaskResponse updateTaskEpic(Integer taskId, UpdateTaskEpicRequest request) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Integer newEpicId = request.getEpicId();

        if (newEpicId == null) {
            // Trường hợp 1: GỠ EPIC KHỎI TASK
            task.setEpic(null);

        } else {
            // Trường hợp 2: GÁN EPIC VÀO TASK
            Epic epic = epicRepository.findById(newEpicId)
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found with ID: " + newEpicId));

            // *** KIỂM TRA TÍNH TOÀN VẸN DỮ LIỆU ***
            // Task và Epic phải thuộc cùng một Project
            if (!task.getProject().getId().equals(epic.getProject().getId())) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException(
                    "Cannot assign this Epic. Epic and Task must belong to the same Project."
                );
            }

            // Gán Epic mới
            task.setEpic(epic);
        }

        // 3. Lưu và trả về
        Task updatedTask = taskRepository.save(task);

        // Giả định bạn có hàm mapToTaskResponse
        return mapToTaskResponse(updatedTask);
    }

    // ======================================================
    // 6. XEM CHI TIẾT TASK
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskDetails(Integer taskId) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // 2. Map sang DTO chi tiết
        return mapToTaskResponse(task);
    }
    // Delete Task
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "TASK", description = "Delete task")
    public void deleteTask(Integer taskId) {
        // 1. Kiểm tra Task có tồn tại không
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }

        // 2. Thực hiện Xóa Vĩnh Viễn
        taskRepository.deleteById(taskId);
    }
    // ======================================================
    // 7. LƯU TRỮ TASK
    // ======================================================
    @Override
    @Transactional
    public void archiveTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        
        // Có thể thêm logic kiểm tra quyền ở đây hoặc ở Controller
        task.setIsArchived(true);
        taskRepository.save(task);
    }

    // ======================================================
    // 6. KHÔI PHỤC TASK
    // ======================================================
    @Override
    @Transactional
    public void restoreTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        
        task.setIsArchived(false);
        taskRepository.save(task);
    }

    // ======================================================
    // 7. LƯU TRỮ TASK
    // ======================================================
    @Override
    @Transactional
    public void archiveTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        
        // Có thể thêm logic kiểm tra quyền ở đây hoặc ở Controller
        task.setIsArchived(true);
        taskRepository.save(task);
    }

    // ======================================================
    // 6. KHÔI PHỤC TASK
    // ======================================================
    @Override
    @Transactional
    public void restoreTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        
        task.setIsArchived(false);
        taskRepository.save(task);
    }

    // ======================================================
    // ⚙️ HÀM HELPER MAPPING
    // ======================================================

    /**
     * Helper: Map Task Entity sang TaskResponse DTO (Chi tiết - Nâng cao).
     */
    @Override
    public TaskResponse mapToTaskResponse(Task task) {
        ProjectStatus status = task.getStatus();
        User assigner = task.getAssigner();
        User assignee = task.getAssignee();
        User reviewer = task.getReviewer();
        User createdBy = task.getCreatedBy();
        Project project = task.getProject();
        Sprint sprint = task.getSprint();
        Epic epic = task.getEpic();
        Task parent = task.getParentTask();

        // 1. Xử lý Subtask Summary
        int totalSubtasks = (task.getSubTasks() != null) ? task.getSubTasks().size() : 0;
        int completedSubtasks = (task.getSubTasks() != null) ?
                (int) task.getSubTasks().stream().filter(st -> st.getStatus() == SubTaskStatus.DONE).count() : 0;

        // 2. Xử lý Tags (Chuyển Set<Tag> sang List<TagInfo>)
        List<TaskResponse.TagInfo> tagInfos = new ArrayList<>();
        if (task.getTags() != null) {
            tagInfos = task.getTags().stream()
                    .map(tag -> TaskResponse.TagInfo.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build())
                    .collect(Collectors.toList());
        }

        // 3. Build DTO
        return TaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .priority(task.getPriority())

                // --- Nested Objects ---
                .status(status != null ? TaskResponse.StatusInfo.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .color(status.getColor())
                        .isCompleted(status.getIsCompletedStatus())
                        .build() : null)

                .project(project != null ? TaskResponse.ProjectInfo.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .build() : null)

                .sprint(sprint != null ? TaskResponse.SprintInfo.builder()
                        .id(sprint.getId())
                        .name(sprint.getName())
                        .build() : null)

                .epic(epic != null ? TaskResponse.EpicInfo.builder()
                        .id(epic.getId())
                        .name(epic.getName())
                        .color(epic.getColor())
                        .build() : null)
                
                .parentTask(parent != null ? TaskResponse.TaskInfo.builder()
                        .id(parent.getId())
                        .taskCode(parent.getTaskCode())
                        .title(parent.getTitle())
                        .build() : null)

                // --- Users ---
                .assignee(assignee != null ? TaskResponse.UserInfo.builder()
                        .id(assignee.getId())
                        .name(assignee.getFullName())
                        .avatarUrl(assignee.getAvatarUrl())
                        .build() : null)

                .assigner(assigner != null ? TaskResponse.UserInfo.builder()
                        .id(assigner.getId())
                        .name(assigner.getFullName())
                        .avatarUrl(assigner.getAvatarUrl())
                        .build() : null)
                
                .reviewer(reviewer != null ? TaskResponse.UserInfo.builder()
                        .id(reviewer.getId())
                        .name(reviewer.getFullName())
                        .avatarUrl(reviewer.getAvatarUrl())
                        .build() : null)

                // --- Extra Info ---
                .tags(tagInfos)
                .subtaskSummary(TaskResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())

                // --- Metrics ---
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours())
                .loggedHours(task.getLoggedHours())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())

                // --- Audit ---
                .createdById(createdBy != null ? createdBy.getId() : null)
                .createdByName(createdBy != null ? createdBy.getFullName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * Helper: Map Task Entity sang TaskSummaryResponse DTO (Cấu trúc Nested).
     */
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus();

        // 1. Xử lý Subtask Summary
        int totalSubtasks = 0;
        int completedSubtasks = 0;
        if (task.getSubTasks() != null) {
            totalSubtasks = task.getSubTasks().size();
            completedSubtasks = (int) task.getSubTasks().stream()
                    .filter(st -> st.getStatus() == SubTaskStatus.DONE) // Giả sử trạng thái hoàn thành là DONE
                    .count();
        }

        // 2. Xử lý Tags
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

        // 3. Build DTO
        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .storyPoints(task.getStoryPoints())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())

                // Mapping Status Object
                .status(status != null ? TaskSummaryResponse.StatusInfo.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .color(status.getColor())
                        .build() : null)

                // Mapping Epic Object
                .epic(epic != null ? TaskSummaryResponse.EpicInfo.builder()
                        .id(epic.getId())
                        .name(epic.getName())
                        .color(epic.getColor())
                        .build() : null)

                // Mapping Assignee Object
                .assignee(assignee != null ? TaskSummaryResponse.UserInfo.builder()
                        .id(assignee.getId())
                        .name(assignee.getFullName())
                        .avatarUrl(assignee.getAvatarUrl())
                        .build() : null)

                // Mapping Tags List
                .tags(tagInfos)

                // Mapping Subtask Summary
                .subtaskSummary(TaskSummaryResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())
                .build();
    }
}