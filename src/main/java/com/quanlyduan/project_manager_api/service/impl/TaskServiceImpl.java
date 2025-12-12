// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
 
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.TaskImportCsvRow;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ImportResultResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskImportPreviewResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskCommentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final EpicRepository epicRepository;
    private final ProjectStatusRepository projectStatusRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public TaskServiceImpl(TaskRepository taskRepository,
                            TaskCommentRepository taskCommentRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           SprintRepository sprintRepository,
                           UserRepository userRepository,
                           EpicRepository epicRepository,
                           SecurityService securityService,
                           ProjectStatusRepository projectStatusRepository) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
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
    public TaskResponse updateTaskSprint(Integer taskId, Integer newSprintId, Integer newSortOrder) {
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

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
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
    public TaskResponse moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Integer projectId = task.getProject().getId();
        Integer newStatusId = request.getNewStatusId();

        ProjectStatus newStatus = projectStatusRepository.findById(newStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + newStatusId));

        if (!newStatus.getProject().getId().equals(projectId)) {
            throw new BadRequestException("New status does not belong to the task's project.");
        }

        // Logic Sort Order
        Integer newSortOrder = request.getNewSortOrder();
        if (newSortOrder == null) {
            newSortOrder = taskRepository.findMaxSortOrderByStatusId(projectId, newStatusId) + 1;
        } else {
            taskRepository.shiftSortOrderInStatus(projectId, newStatusId, newSortOrder);
        }

        // --- LOGIC GHI LOG CHI TIẾT ---
        String oldStatusName = (task.getStatus() != null) ? task.getStatus().getName() : "None";
        String newStatusName = newStatus.getName();

        // Chỉ ghi log nếu trạng thái thực sự thay đổi
        if (!oldStatusName.equals(newStatusName)) {
             ActivityLogContext.setDetail(String.format(
                "changed status from <strong>%s</strong> to <strong>%s</strong>", 
                oldStatusName, newStatusName
            ));
        } else {
             ActivityLogContext.setDetail("reordered in <strong>" + newStatusName + "</strong> column");
        }
        // ------------------------------

        task.setStatus(newStatus);
        task.setSortOrder(newSortOrder);

        if (newStatus.getIsCompletedStatus()) {
            task.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    // ======================================================
    // 4. CẬP NHẬT TASK (UPDATE ALL FIELDS) - FULL CODE
    // ======================================================

    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "TASK", description = "Update task information")
    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        
        Integer projectId = task.getProject().getId();
        StringBuilder changes = new StringBuilder();

        // ==========================================
        // 1. SCALAR FIELDS (So sánh an toàn)
        // ==========================================

        // Title
        if (request.getTitle() != null && !request.getTitle().isBlank() 
                && !request.getTitle().equals(task.getTitle())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", task.getTitle(), request.getTitle()));
            task.setTitle(request.getTitle());
        }

        // Description
        // Lưu ý: Swagger hay gửi chuỗi "string" mặc định, ta nên check khác "string" nếu muốn kỹ hơn
        if (request.getDescription() != null 
                && !request.getDescription().equals(task.getDescription())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated description");
            task.setDescription(request.getDescription());
        }

        // Task Type
        if (request.getTaskType() != null && request.getTaskType() != task.getTaskType()) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed type to <strong>%s</strong>", request.getTaskType()));
            task.setTaskType(request.getTaskType());
        }

        // Priority
        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed priority to <strong>%s</strong>", request.getPriority()));
            task.setPriority(request.getPriority());
        }

        // Story Points (So sánh số)
        if (request.getStoryPoints() != null && !Objects.equals(request.getStoryPoints(), task.getStoryPoints())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed points to <strong>%d</strong>", request.getStoryPoints()));
            task.setStoryPoints(request.getStoryPoints());
        }

        // Estimated Hours (So sánh BigDecimal an toàn: dùng compareTo để 10.0 == 10.00)
        if (request.getEstimatedHours() != null) {
            boolean isDifferent;
            if (task.getEstimatedHours() == null) isDifferent = true;
            else isDifferent = request.getEstimatedHours().compareTo(task.getEstimatedHours()) != 0;

            if (isDifferent) {
                if (changes.length() > 0) changes.append(", ");
                changes.append(String.format("changed estimate to <strong>%s</strong>h", request.getEstimatedHours()));
                task.setEstimatedHours(request.getEstimatedHours());
            }
        }

        // Dates (Quan trọng: Sử dụng isEqual để so sánh thời gian)
        if (request.getStartDate() != null) {
            boolean isDifferent = (task.getStartDate() == null) || !request.getStartDate().isEqual(task.getStartDate());
            if (isDifferent) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("changed start date");
                task.setStartDate(request.getStartDate());
            }
        }

        if (request.getDueDate() != null) {
            boolean isDifferent = (task.getDueDate() == null) || !request.getDueDate().isEqual(task.getDueDate());
            if (isDifferent) {
                if (changes.length() > 0) changes.append(", ");
                changes.append("changed due date");
                task.setDueDate(request.getDueDate());
            }
        }

        // ==========================================
        // 2. RELATIONS (Quan hệ)
        // ==========================================

        // A. Status
        if (request.getStatusId() != null) {
            Integer oldStatusId = task.getStatus() != null ? task.getStatus().getId() : 0;
            if (!request.getStatusId().equals(oldStatusId)) {
                ProjectStatus newStatus = projectStatusRepository.findById(request.getStatusId())
                        .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
                if (!newStatus.getProject().getId().equals(projectId)) throw new BadRequestException("Status invalid");

                if (changes.length() > 0) changes.append(", ");
                String oldName = task.getStatus() != null ? task.getStatus().getName() : "None";
                changes.append(String.format("changed status from <strong>%s</strong> to <strong>%s</strong>", oldName, newStatus.getName()));
                
                task.setStatus(newStatus);
            }
        }

        // B. Sprint
        if (request.getSprintId() != null) {
            Integer oldSprintId = task.getSprint() != null ? task.getSprint().getId() : 0;
            // Nếu gửi lên 0 (muốn gỡ sprint) và hiện tại đang có sprint (id khác 0) -> Thay đổi
            // Nếu gửi lên X, hiện tại là Y -> Thay đổi
            if (!request.getSprintId().equals(oldSprintId)) {
                if (request.getSprintId() == 0) {
                    if (changes.length() > 0) changes.append(", ");
                    changes.append("moved to <strong>Backlog</strong>");
                    task.setSprint(null);
                } else {
                    Sprint sprint = sprintRepository.findById(request.getSprintId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
                    if (!sprint.getProject().getId().equals(projectId)) throw new BadRequestException("Sprint invalid");

                    if (changes.length() > 0) changes.append(", ");
                    changes.append(String.format("moved to sprint <strong>%s</strong>", sprint.getName()));
                    task.setSprint(sprint);
                }
            }
        }

        // C. Epic
        if (request.getEpicId() != null) {
             Integer oldEpicId = task.getEpic() != null ? task.getEpic().getId() : 0;
             if (!request.getEpicId().equals(oldEpicId)) {
                 if (request.getEpicId() == 0) {
                     if (changes.length() > 0) changes.append(", ");
                     changes.append("removed from Epic");
                     task.setEpic(null);
                 } else {
                     Epic epic = epicRepository.findById(request.getEpicId())
                             .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
                     if (!epic.getProject().getId().equals(projectId)) throw new BadRequestException("Epic invalid");

                     if (changes.length() > 0) changes.append(", ");
                     changes.append(String.format("added to epic <strong>%s</strong>", epic.getName()));
                     task.setEpic(epic);
                 }
             }
        }

        // D. Assignee
        if (request.getAssigneeId() != null) {
             Integer oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : 0;
             if (!request.getAssigneeId().equals(oldAssigneeId)) {
                 if (request.getAssigneeId() == 0) {
                     if (changes.length() > 0) changes.append(", ");
                     changes.append("unassigned");
                     task.setAssignee(null);
                 } else {
                     User assignee = userRepository.findById(request.getAssigneeId())
                             .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                     
                     if (changes.length() > 0) changes.append(", ");
                     changes.append(String.format("assigned to <strong>%s</strong>", assignee.getFullName()));
                     task.setAssignee(assignee);
                 }
             }
        }

        // ==========================================
        // 3. SET LOG & SAVE
        // ==========================================

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
            // ActivityLogContext.setDetail("updated details");
        }

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    // ======================================================
    // 5. GÁN/GỠ EPIC VÀO TASK
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "TASK", description = "Update Task Epic")
    public TaskResponse updateTaskEpic(Integer taskId, UpdateTaskEpicRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Integer newEpicId = request.getEpicId();
        
        // --- LOGIC LOG CHI TIẾT ---
        String oldEpicName = task.getEpic() != null ? task.getEpic().getName() : "None";
        String newEpicName = "None";
        // --------------------------

        if (newEpicId == null || newEpicId == 0) {
            // Trường hợp 1: GỠ EPIC
            task.setEpic(null);
            ActivityLogContext.setDetail("removed from epic <strong>" + oldEpicName + "</strong>");

        } else {
            // Trường hợp 2: GÁN EPIC
            Epic epic = epicRepository.findById(newEpicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found with ID: " + newEpicId));

            if (!task.getProject().getId().equals(epic.getProject().getId())) {
                throw new BadRequestException("Epic does not belong to this project.");
            }
            
            newEpicName = epic.getName();
            task.setEpic(epic);
            
            ActivityLogContext.setDetail(String.format(
                "changed epic from <strong>%s</strong> to <strong>%s</strong>", 
                oldEpicName, newEpicName
            ));
        }

        Task updatedTask = taskRepository.save(task);
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
    public TaskResponse deleteTask(Integer taskId) {
        // BƯỚC 1: Tìm Task trước (Để lấy dữ liệu trả về và để xóa)
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // BƯỚC 2: Map sang Response DTO (Lưu lại thông tin trước khi xóa)
        // Lưu ý: Phải map trước khi xóa các quan hệ con nếu cần thông tin con
        TaskResponse response = mapToTaskResponse(task);

        // BƯỚC 3: XỬ LÝ LỖI "Row was updated or deleted..." (Vấn đề cũ của bạn)
        // Xóa thủ công comments trước để tránh xung đột Hibernate
        if (task.getComments() != null && !task.getComments().isEmpty()) {
            // Cần inject TaskCommentRepository ở trên Constructor
             taskCommentRepository.deleteAll(task.getComments());
             
             // Xóa list trong memory để Hibernate không bị loạn
             task.setComments(new ArrayList<>()); 
        }

        // BƯỚC 4: Xóa Task
        // Dùng delete(entity) thay vì deleteById(id) để tận dụng object đã load
        taskRepository.delete(task);

        // BƯỚC 5: Trả về thông tin task vừa xóa
        return response;
    }
    @Override
    @Transactional
    @LogActivity(action = "IMPORT", entityType = "TASK", description = "Bulk import tasks from CSV")
    public ImportResultResponse importTasksFromCsv(Integer projectId, MultipartFile file) {
        
        // 1. TÌM PROJECT & VALIDATE HIERARCHY
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        
        Integer workspaceId = project.getWorkspace().getId();
        Integer companyId = project.getWorkspace().getCompany().getId();
        // 2. PARSE CSV
        List<TaskImportCsvRow> csvRows;
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Sử dụng Reader có định dạng UTF-8 để tránh lỗi font hoặc BOM
            CsvToBean<TaskImportCsvRow> csvToBean = new CsvToBeanBuilder<TaskImportCsvRow>(reader)
                    .withType(TaskImportCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreQuotations(false)
                    .withSeparator(',') // Bắt buộc file phải ngăn cách bằng dấu phẩy
                    .build();
            csvRows = csvToBean.parse();
        } catch (Exception e) {
            // Log lỗi ra để debug
            e.printStackTrace();
            throw new BadRequestException("Failed to parse CSV file. Please ensure columns are separated by commas (,) and headers match exactly. Error: " + e.getMessage());
        }

        if (csvRows.isEmpty()) {
            throw new BadRequestException("CSV file is empty.");
        }

        // =================================================================
        // 3. PREPARE DATA (BULK FETCH)
        // =================================================================
        
        Map<String, User> projectMembersMap = projectMemberRepository.findByProject_Id(projectId, Pageable.unpaged())
                    .stream()
                    .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                    .map(ProjectMember::getUser)
                    .collect(Collectors.toMap(
                            (User u) -> u.getEmail().toLowerCase(),
                            (User u) -> u
                    ));

        Map<String, ProjectStatus> statusMap = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId)
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase().trim(),
                        s -> s
                ));

        ProjectStatus defaultStatus = statusMap.values().stream()
                .min(Comparator.comparingInt(ProjectStatus::getSortOrder))
                .orElseThrow(() -> new ResourceNotFoundException("Project has no statuses configured."));

        long currentSortOrder = taskRepository.countByProjectId(projectId);
        User creator = securityService.getCurrentAuthenticatedUser();

        // =================================================================
        // 4. VALIDATE & MAP ROWS
        // =================================================================
        List<Task> validTasks = new ArrayList<>();
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int rowIndex = 1;

        for (TaskImportCsvRow row : csvRows) {
            rowIndex++; 
            
            // Validate Title
            if (row.getTitle() == null || row.getTitle().trim().isEmpty()) {
                errors.add(new ImportResultResponse.ImportError(rowIndex, "Title", "Task title is required."));
                continue; 
            }

            // Map Assignee
            User assignee = null;
            if (row.getAssigneeEmail() != null && !row.getAssigneeEmail().trim().isEmpty()) {
                String email = row.getAssigneeEmail().trim().toLowerCase();
                assignee = projectMembersMap.get(email);
                
                if (assignee == null) {
                    errors.add(new ImportResultResponse.ImportError(rowIndex, "Assignee Email", 
                        "User with email '" + email + "' is not a member of this project."));
                    continue; 
                }
            }

            // Parse Date (NÂNG CẤP: Hỗ trợ nhiều định dạng)
            LocalDateTime dueDate = null;
            if (row.getDueDate() != null && !row.getDueDate().trim().isEmpty()) {
                dueDate = parseDateFlexible(row.getDueDate().trim());
                if (dueDate == null) {
                    errors.add(new ImportResultResponse.ImportError(rowIndex, "Due Date", 
                        "Invalid date format: '" + row.getDueDate() + "'. Supported: yyyy-MM-dd, MM/dd/yyyy, dd/MM/yyyy"));
                    continue;
                }
            }

            // Map Status
            ProjectStatus status = defaultStatus;
            if (row.getStatusName() != null && statusMap.containsKey(row.getStatusName().trim().toLowerCase())) {
                status = statusMap.get(row.getStatusName().trim().toLowerCase());
            }

            // Map Priority
            TaskPriority priority = TaskPriority.MEDIUM;
            if (row.getPriority() != null && !row.getPriority().isBlank()) {
                try {
                    priority = TaskPriority.valueOf(row.getPriority().trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                     errors.add(new ImportResultResponse.ImportError(rowIndex, "Priority", "Invalid priority. Use HIGH, MEDIUM, LOW, URGENT."));
                     continue;
                }
            }

            // Build Entity
            if (errors.isEmpty()) {
                 Task.TaskBuilder taskBuilder = Task.builder()
                    .project(project)
                    .title(row.getTitle())
                    .description(row.getDescription())
                    .assignee(assignee)
                    .priority(priority)
                    .status(status)
                    .taskType(TaskType.TASK)
                    .dueDate(dueDate)
                    .storyPoints(row.getStoryPoints())
                    .assigner(creator)
                    .createdBy(creator);
                 
                 if (row.getEstimatedHours() != null) {
                     taskBuilder.estimatedHours(BigDecimal.valueOf(row.getEstimatedHours()));
                 }
                 
                 validTasks.add(taskBuilder.build());
            }
        }

        // 5. SAVE OR RETURN ERRORS
        if (!errors.isEmpty()) {
            return ImportResultResponse.builder()
                    .totalRows(csvRows.size())
                    .successCount(0)
                    .errorCount(errors.size())
                    .errors(errors)
                    .build();
        }

        // 6. FINAL SAVE
        long finalSortOrder = currentSortOrder;
        for (Task task : validTasks) {
             finalSortOrder++;
             task.setSortOrder((int) finalSortOrder);
             task.setTaskCode(project.getProjectCode() + "-" + finalSortOrder);
        }

        taskRepository.saveAll(validTasks);

        return ImportResultResponse.builder()
                .totalRows(csvRows.size())
                .successCount(validTasks.size())
                .errorCount(0)
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * Hỗ trợ parse nhiều kiểu ngày tháng:
     * 1. 2025-12-20 (Chuẩn ISO)
     * 2. 12/20/2025 (Kiểu Mỹ - Excel hay dùng)
     * 3. 20/12/2025 (Kiểu Việt Nam)
     */
    private LocalDateTime parseDateFlexible(String dateStr) {
        String[] patterns = {
            "yyyy-MM-dd", 
            "M/d/yyyy",   // Xử lý 12/20/2025 hoặc 1/5/2026
            "MM/dd/yyyy", 
            "d/M/yyyy",   // Xử lý 20/12/2025
            "dd/MM/yyyy"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // Thử pattern tiếp theo
            }
        }
        return null; // Không parse được
    }
    @Override
@Transactional(readOnly = true)
public List<TaskImportPreviewResponse> previewImportTasks(Integer projectId, MultipartFile file) {
    // 1. Parse CSV (Giống logic cũ)
    List<TaskImportCsvRow> csvRows;
    try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
        CsvToBean<TaskImportCsvRow> csvToBean = new CsvToBeanBuilder<TaskImportCsvRow>(reader)
                .withType(TaskImportCsvRow.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreQuotations(false)
                .build();
        csvRows = csvToBean.parse();
    } catch (Exception e) {
        throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
    }

    // 2. Prepare Data (Bulk Fetch) - Tái sử dụng logic cũ
    // Lấy Map Members và Map Statuses ở đây... (Code giống bài trước)
    Map<String, User> projectMembersMap = projectMemberRepository.findByProject_Id(projectId, Pageable.unpaged())
            .stream().filter(pm -> pm.getStatus() == MemberStatus.ACTIVE).map(ProjectMember::getUser)
            .collect(Collectors.toMap(u -> u.getEmail().toLowerCase(), u -> u));
    
    // 3. Validate từng dòng và map sang Preview DTO
    List<TaskImportPreviewResponse> previewList = new ArrayList<>();
    int index = 0;

    for (TaskImportCsvRow row : csvRows) {
        index++;
        List<String> errors = new ArrayList<>();

        // Validate Title
        if (row.getTitle() == null || row.getTitle().trim().isEmpty()) {
            errors.add("Title is required.");
        }

        // Validate Email
        if (row.getAssigneeEmail() != null && !row.getAssigneeEmail().isBlank()) {
            if (!projectMembersMap.containsKey(row.getAssigneeEmail().trim().toLowerCase())) {
                errors.add("User '" + row.getAssigneeEmail() + "' is not in project.");
            }
        }

        // Validate Date Format (Check sơ bộ)
        if (row.getDueDate() != null && !row.getDueDate().isBlank()) {
            try {
                LocalDate.parse(row.getDueDate().trim());
            } catch (DateTimeParseException e) {
                errors.add("Invalid date (yyyy-MM-dd).");
            }
        }
        
        // ... Các validate khác (Priority, Status) nếu cần ...

        previewList.add(TaskImportPreviewResponse.builder()
                .rowIndex(index)
                .title(row.getTitle())
                .description(row.getDescription())
                .assigneeEmail(row.getAssigneeEmail())
                .priority(row.getPriority())
                .statusName(row.getStatusName())
                .dueDate(row.getDueDate())
                .storyPoints(row.getStoryPoints())
                .estimatedHours(row.getEstimatedHours())
                .isValid(errors.isEmpty())
                .errors(errors)
                .build());
    }
    return previewList;
}

// =================================================================
// LOGIC SAVE (BƯỚC 2 - Nhận JSON đã sửa từ FE)
// =================================================================
@Override
@Transactional
@LogActivity(action = "IMPORT", entityType = "TASK", description = "Import tasks from JSON")
public ImportResultResponse saveImportedTasks(Integer projectId, List<TaskImportPreviewResponse> rows) {
    // 1. Prepare Data Maps (Cache for performance)
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            
    Map<String, User> memberMap = projectMemberRepository.findByProject_Id(projectId, Pageable.unpaged())
            .stream()
            .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
            .map(ProjectMember::getUser)
            .collect(Collectors.toMap(u -> u.getEmail().toLowerCase(), u -> u));
    
    Map<String, ProjectStatus> statusMap = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId)
            .stream().collect(Collectors.toMap(s -> s.getName().toLowerCase(), s -> s));
            
    // Fallback default status
    ProjectStatus defaultStatus = statusMap.values().stream()
            .min(Comparator.comparingInt(ProjectStatus::getSortOrder))
            .orElseThrow(() -> new ResourceNotFoundException("No status found"));

    User creator = securityService.getCurrentAuthenticatedUser();
    long currentSort = taskRepository.countByProjectId(projectId);
    List<Task> tasksToSave = new ArrayList<>();

    // 2. Processing Rows (Lenient Mode)
    for (TaskImportPreviewResponse row : rows) {
        // Skip only if Title is missing (Mandatory field)
        if (row.getTitle() == null || row.getTitle().trim().isEmpty()) {
            continue; 
        }

        // --- LENIENT MAPPING LOGIC ---

        // 1. Assignee: Map if exists, else NULL
        User assignee = null;
        if (row.getAssigneeEmail() != null && !row.getAssigneeEmail().isBlank()) {
            // Try to find user. If map returns null (not found), assignee remains null.
            assignee = memberMap.get(row.getAssigneeEmail().trim().toLowerCase());
        }

        // 2. Status: Map if exists, else DEFAULT
        ProjectStatus status = defaultStatus;
        if (row.getStatusName() != null && statusMap.containsKey(row.getStatusName().trim().toLowerCase())) {
            status = statusMap.get(row.getStatusName().trim().toLowerCase());
        }

        // 3. Priority: Map if valid, else MEDIUM
        TaskPriority priority = TaskPriority.MEDIUM;
        try {
            if (row.getPriority() != null) {
                priority = TaskPriority.valueOf(row.getPriority().toUpperCase());
            }
        } catch (Exception ignored) {
            // Invalid priority -> Fallback to MEDIUM (or NULL if your DB allows)
        }

        // 4. Date: Map if valid, else NULL
        LocalDateTime dueDate = null;
        try {
            if (row.getDueDate() != null && !row.getDueDate().isBlank()) {
                dueDate = LocalDate.parse(row.getDueDate()).atStartOfDay();
            }
        } catch (Exception ignored) {
            // Invalid date format -> Set to NULL (Safe)
        }

        // --- BUILD ENTITY ---
        Task task = Task.builder()
                .project(project)
                .taskCode(project.getProjectCode() + "-" + (++currentSort))
                .title(row.getTitle())
                .description(row.getDescription())
                .assignee(assignee) // Will be User or Null
                .status(status)     // Will be Selected or Default
                .priority(priority) // Will be Selected or Medium
                .taskType(TaskType.TASK)
                .dueDate(dueDate)   // Will be Date or Null
                .storyPoints(row.getStoryPoints())
                .estimatedHours(row.getEstimatedHours() != null ? BigDecimal.valueOf(row.getEstimatedHours()) : null)
                .sortOrder((int)currentSort)
                .createdBy(creator)
                .assigner(creator)
                .build();
        
        tasksToSave.add(task);
    }

    // 3. Batch Save
    taskRepository.saveAll(tasksToSave);
    
    return ImportResultResponse.builder()
            .successCount(tasksToSave.size())
            .totalRows(rows.size())
            .errorCount(0)
            .errors(Collections.emptyList())
            .build();
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
                .workspaceId(task.getProject().getWorkspace().getId())
                .companyId(task.getProject().getWorkspace().getCompany().getId())
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
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .workspaceId(task.getProject().getWorkspace().getId())
                .companyId(task.getProject().getWorkspace().getCompany().getId())
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