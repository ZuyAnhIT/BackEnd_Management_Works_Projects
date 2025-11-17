// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
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
    private final ProjectMemberRepository projectMemberRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           SprintRepository sprintRepository,
                           UserRepository userRepository,
                           EpicRepository epicRepository,
                           SecurityService securityService,
                           ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.epicRepository = epicRepository;
        this.projectMemberRepository = projectMemberRepository;
    }
     // US-S3-7: Kéo/thả Task vào Sprint
    @Override
    @Transactional
    public void updateTaskSprint(Integer taskId, Integer newSprintId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (newSprintId == null) {
            // Kéo về Backlog
            task.setSprint(null);
        } else {
            // Kéo vào 1 Sprint
            Sprint sprint = sprintRepository.findById(newSprintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            
            // Validate: Task và Sprint phải cùng Project
            if (!task.getProject().getId().equals(sprint.getProject().getId())) {
                throw new BadRequestException("Task and Sprint do not belong to the same project");
            }
            task.setSprint(sprint);
        }
        
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskAssignee(Integer taskId, Integer assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(task.getProject().getId(), assignee.getId());
            if (!isMember) {
                throw new BadRequestException("User is not a member of this project");
            }
        }

        User currentUser = securityService.getCurrentAuthenticatedUser();
        task.setAssignee(assignee);
        task.setAssigner(currentUser);

        Task saved = taskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    // === HÀM HELPER MAPPING ===
    @Override
    public TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType().name())
                .status(task.getStatus())
                .priority(task.getPriority().name())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .epicId(task.getEpic() != null ? task.getEpic().getId() : null)
                .assignerId(task.getAssigner() != null ? task.getAssigner().getId() : null)
                .assignerName(task.getAssigner() != null ? task.getAssigner().getFullName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .assigneeAvatar(task.getAssignee() != null ? task.getAssignee().getAvatarUrl() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getFullName())
                .createdAt(task.getCreatedAt())
                .build();
    }

    // LOGIC TAO TASK MOI
    @Override
    @Transactional
    public TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request) {
        
        // 1. Lấy thông tin người tạo (từ SecurityContext)
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // 2. Lấy dự án (Project)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        // 3. (Tùy chọn) Lấy Sprint và Epic nếu có
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint với ID: " + request.getSprintId()));
            // (Nên kiểm tra sprint này có thuộc project này không)
        }
        
        Epic epic = null;
        if (request.getEpicId() != null) {
            epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Epic với ID: " + request.getEpicId()));
            // (Nên kiểm tra epic này có thuộc project này không)
        }
        
        // 4. (Tùy chọn) Lấy người được gán (assignee)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng (assignee) với ID: " + request.getAssigneeId()));
            // (Nên kiểm tra assignee này có phải là thành viên dự án không)
        }

        // 5. TODO: Logic tạo Task Code tự động (ví dụ: WEB-4)
        String newCode = project.getProjectCode() + "-" + (taskRepository.countByProjectId(projectId) + 1);

        // 6. Tạo Task Entity
        Task newTask = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskCode(newCode) // Dùng mã tự động
                .taskType(request.getTaskType())
                .status(request.getStatus() != null ? request.getStatus() : "TO_DO") // Mặc định là TO_DO
                .priority(request.getPriority())
                .sprint(sprint)
                .epic(epic)
                .assignee(assignee)
                .assigner(creator) // Người gán task
                .createdBy(creator) // Người tạo task
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .sortOrder(0) // Mặc định
                .build();
        
        Task savedTask = taskRepository.save(newTask);
        
        // 7. Map sang DTO và trả về
        return mapToTaskSummaryResponse(savedTask);
    }

    /**
     * Hàm helper để map Task (Entity) sang TaskSummaryResponse (DTO)
     * (Tái sử dụng từ ProjectServiceImpl)
     */
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                .status(task.getStatus())
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
}
