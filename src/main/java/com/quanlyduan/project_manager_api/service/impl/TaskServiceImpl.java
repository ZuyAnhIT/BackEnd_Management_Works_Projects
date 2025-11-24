// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
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
    
    // LOGIC KEO THA TASK (SPRINT + VI TRI)
    @Override
    @Transactional
    public void updateTaskSprint(Integer taskId, Integer newSprintId, Integer newSortOrder) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc"));

        Integer projectId = task.getProject().getId();

        // 2. Xác định Sprint đích (hoặc Backlog)
        Sprint targetSprint = null;
        if (newSprintId != null) {
            targetSprint = sprintRepository.findById(newSprintId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint"));
            if (!targetSprint.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Task và Sprint không cùng dự án");
            }
        }

        // 3. Xác định vị trí (SortOrder)
        // Nếu người dùng không gửi vị trí -> Mặc định xuống cuối cùng
        if (newSortOrder == null) {
            if (targetSprint != null) {
                newSortOrder = taskRepository.findMaxSortOrderBySprintId(newSprintId) + 1;
            } else {
                newSortOrder = taskRepository.findMaxSortOrderByProjectIdAndSprintIsNull(projectId) + 1;
            }
        } else {
            // Nếu có vị trí cụ thể -> Phải đẩy các task đang đứng đó lùi xuống
            if (targetSprint != null) {
                taskRepository.shiftSortOrderInSprint(newSprintId, newSortOrder);
            } else {
                taskRepository.shiftSortOrderInBacklog(projectId, newSortOrder);
            }
        }

        // 4. Cập nhật Task
        task.setSprint(targetSprint);
        task.setSortOrder(newSortOrder);
        
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

    // LOGIC TAO TASK MOI (HO TRO QUICK CREATE)
    @Override
    @Transactional
    public TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request) {
        
        // 1. Lấy thông tin người tạo
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // 2. Lấy dự án
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án")); // Đã dịch

        // 3. TỰ ĐỘNG TÌM TRẠNG THÁI (CỘT) MẶC ĐỊNH (Vị trí đầu tiên)
        ProjectStatus defaultStatus = projectStatusRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án này chưa cấu hình bảng trạng thái (Board Columns).")); // Đã dịch
        
        // 4. XỬ LÝ SPRINT (Quan trọng cho 2 trường hợp)
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint với ID: " + request.getSprintId())); // Đã dịch
            
            // Validate: Sprint phải thuộc Project này
            if (!sprint.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Sprint không thuộc về dự án này."); // Đã dịch
            }
        }
        // Nếu request.getSprintId() == null thì sprint = null (nghĩa là nằm ở Backlog)

        // 5. Xử lý các trường tùy chọn (Epic, Assignee)
        Epic epic = null;
        if (request.getEpicId() != null) {
            epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Epic"));
        }
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người được giao việc"));
        }

        // 6. SINH MÃ TASK TỰ ĐỘNG (Ví dụ: WEB-1, WEB-2)
        long taskCount = taskRepository.countByProjectId(projectId);
        String newCode = project.getProjectCode() + "-" + (taskCount + 1);

        // 7. Tạo Task Entity (Gán mặc định nếu thiếu)
        Task newTask = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription()) // Có thể null
                .taskCode(newCode)
                
                // Mặc định là TASK nếu không chọn
                .taskType(request.getTaskType() != null ? request.getTaskType() : TaskType.TASK)
                
                // Mặc định vào cột đầu tiên (To Do / Backlog Column)
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


    // === HÀM HELPER 
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

    // LOGIC: XEM CHI TIET TASK
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskDetails(Integer taskId) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId)); // Đã dịch
        
        // (Bảo mật đã được xử lý ở Controller)

        // 2. Map sang DTO chi tiết
        return mapToTaskResponse(task);
    }

    // LOGIC: CAP NHAT TASK
    @Override
    @Transactional
    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request) {
        // 1. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId)); // Đã dịch
        
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
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái"));
            if (!newStatus.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Trạng thái không thuộc về dự án này");
            }
            task.setStatus(newStatus);
        }

        // B. Sprint
        if (request.getSprintId() != null) {
            if (request.getSprintId() == 0) {
                task.setSprint(null); // Gỡ bỏ sprint (về backlog)
            } else {
                Sprint sprint = sprintRepository.findById(request.getSprintId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint"));
                if (!sprint.getProject().getId().equals(projectId)) {
                    throw new BadRequestException("Sprint không thuộc về dự án này");
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
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Epic"));
                if (!epic.getProject().getId().equals(projectId)) {
                    throw new BadRequestException("Epic không thuộc về dự án này");
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
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                // (Nên check xem user có trong Project không, nhưng tạm thời bỏ qua để đơn giản)
                task.setAssignee(assignee);
            }
        }

        // 4. Lưu và trả về
        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }
}