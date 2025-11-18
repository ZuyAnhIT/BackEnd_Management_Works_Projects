// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskService;

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
     // US-S3-7: Kéo/thả Task vào Sprint
    @Override
    @Transactional
    public void updateTaskSprint(Integer taskId, Integer newSprintId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc")); // Đã dịch

        if (newSprintId == null) {
            // Kéo về Backlog
            task.setSprint(null);
        } else {
            // Kéo vào 1 Sprint
            Sprint sprint = sprintRepository.findById(newSprintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint")); // Đã dịch
            
            // Validate: Task và Sprint phải cùng Project
            if (!task.getProject().getId().equals(sprint.getProject().getId())) {
                throw new BadRequestException("Công việc và Sprint không thuộc cùng một dự án"); // Đã dịch
            }
            task.setSprint(sprint);
        }
        
        taskRepository.save(task);
    }

    // === HÀM HELPER MAPPING ===
    @Override
    public TaskResponse mapToTaskResponse(Task task) {
        ProjectStatus status = task.getStatus(); 
        User assigner = task.getAssigner();
        User assignee = task.getAssignee();
        User createdBy = task.getCreatedBy();
        
        return TaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .description(task.getDescription())
                
                // *** SỬA LỖI LOGIC: Gán các trường status mới ***
                .statusId(status != null ? status.getId() : null)
                .statusName(status != null ? status.getName() : "N/A")
                .statusColor(status != null ? status.getColor() : "#FFFFFF")

                .taskType(task.getTaskType() != null ? task.getTaskType().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours()) // Thêm
                .loggedHours(task.getLoggedHours()) // Thêm
                .startDate(task.getStartDate()) // Thêm
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt()) // Thêm

                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .epicId(task.getEpic() != null ? task.getEpic().getId() : null)
                
                .assignerId(assigner != null ? assigner.getId() : null)
                .assignerName(assigner != null ? assigner.getFullName() : null)
                
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatar(assignee != null ? assignee.getAvatarUrl() : null)
                
                .createdById(createdBy.getId()) // Giả định createdBy không bao giờ null
                .createdByName(createdBy.getFullName())
                .createdAt(task.getCreatedAt())
                .build();
    }

    // LOGIC TAO TASK MOI
    @Override
    @Transactional
    public TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request) {
        
        User creator = securityService.getCurrentAuthenticatedUser();
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        // *** LOGIC MỚI: TÌM TRẠNG THÁI (CỘT) MẶC ĐỊNH ***
        ProjectStatus defaultStatus = projectStatusRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án này chưa có cột trạng thái (status). Vui lòng cấu hình."));
        
        // (Logic tìm Sprint, Epic, Assignee giữ nguyên)
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint với ID: " + request.getSprintId()));
        }
        Epic epic = null;
        if (request.getEpicId() != null) {
            epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Epic với ID: " + request.getEpicId()));
        }
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng (assignee) với ID: " + request.getAssigneeId()));
        }

        // (Logic tạo Task Code giữ nguyên)
        String newCode = project.getProjectCode() + "-" + (taskRepository.countByProjectId(projectId) + 1);

        // 6. Tạo Task Entity
        Task newTask = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskCode(newCode)
                .taskType(request.getTaskType())
                .status(defaultStatus) // *** ĐÃ SỬA: Gán đối tượng ProjectStatus ***
                .priority(request.getPriority())
                .sprint(sprint)
                .epic(epic)
                .assignee(assignee)
                .assigner(creator) 
                .createdBy(creator) 
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .sortOrder(0) 
                .build();
        
        Task savedTask = taskRepository.save(newTask);
        
        // 7. Map sang DTO và trả về
        return mapToTaskSummaryResponse(savedTask);
    }

    // === HÀM HELPER (ĐÃ CẬP NHẬT) ===
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        ProjectStatus status = task.getStatus(); // Lấy đối tượng Status

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                .statusId(status != null ? status.getId() : null)
                .statusName(status != null ? status.getName() : "N/A")
                .statusColor(status != null ? status.getColor() : "#FFFFFF")
                
                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatarUrl(assignee != null ? assignee.getAvatarUrl() : null)
                .epicId(epic != null ? epic.getId() : null)
                .epicName(epic != null ? epic.getName() : null)
                .epicColor(epic != null ? epic.getColor() : null)
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
                .build();
    }

    // LOGIC DI CHUYEN TASK (KEO THA)
    @Override
    @Transactional
    public void moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId)); // Đã dịch

        // 2. Tìm Status mới
        ProjectStatus newStatus = projectStatusRepository.findById(request.getNewStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + request.getNewStatusId())); // Đã dịch

        // 3. Validate: Status mới phải thuộc cùng Project với Task
        // (Tránh trường hợp kéo task của Dự án A vào cột của Dự án B)
        if (!newStatus.getProject().getId().equals(task.getProject().getId())) {
            throw new BadRequestException("Trạng thái mới không thuộc về dự án của công việc này"); // Đã dịch
        }

        // 4. Cập nhật
        task.setStatus(newStatus);
        
        // (Tùy chọn: Nếu cột mới là "DONE", có thể tự động cập nhật completedAt)
        if (newStatus.getIsCompletedStatus()) {
            task.setCompletedAt(java.time.LocalDate.now());
        } else {
            task.setCompletedAt(null); // Nếu kéo ngược lại, xóa ngày hoàn thành
        }

        taskRepository.save(task);
    }
}