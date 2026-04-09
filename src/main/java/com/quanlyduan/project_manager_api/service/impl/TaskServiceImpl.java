package com.quanlyduan.project_manager_api.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ImportTaskResultResponse;
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
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskCommentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    // Khai bao cac hang so thong bao loi
    public static final String ERROR_TASK_NOT_FOUND = "Task not found.";
    public static final String ERROR_TASK_NOT_FOUND_ID = "Task not found with ID: ";
    public static final String ERROR_SPRINT_NOT_FOUND = "Sprint not found.";
    public static final String ERROR_SPRINT_NOT_FOUND_ID = "Sprint not found with ID: ";
    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found.";
    public static final String ERROR_STATUS_NOT_FOUND = "Status not found";
    public static final String ERROR_EPIC_NOT_FOUND = "Epic not found";
    public static final String ERROR_ASSIGNEE_NOT_FOUND = "Assignee user not found";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_PROJECT_MISMATCH_SPRINT = "Task and Sprint must belong to the same project.";
    public static final String ERROR_PROJECT_MISMATCH_STATUS = "New status does not belong to the task's project.";
    public static final String ERROR_PROJECT_MISMATCH_EPIC = "Epic invalid";
    public static final String ERROR_NO_STATUS_BOARD = "This project has no status board configuration.";
    public static final String ERROR_INVALID_EXCEL = "Please upload a valid Excel file (.xlsx)";
    public static final String ERROR_READING_EXCEL = "Error reading Excel: ";
    public static final String ERROR_GENERATE_TEMPLATE = "Fail to generate template: ";

    // Khai bao cac hang so hanh dong log
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_MOVE_STATUS = "MOVE_STATUS";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_IMPORT = "IMPORT";
    public static final String ENTITY_TASK = "TASK";

    public static final String DESC_DRAG_DROP = "Drag and drop Task with Sprint";
    public static final String DESC_CREATE_TASK = "Create new Task";
    public static final String DESC_MOVE_STATUS = "Change task status";
    public static final String DESC_UPDATE_TASK = "Update task information";
    public static final String DESC_UPDATE_EPIC = "Update Task Epic";
    public static final String DESC_DELETE_TASK = "Delete task";
    public static final String DESC_IMPORT_TASK = "Import tasks from JSON";

    // Khai bao cac hang so ghi log chi tiet
    public static final String LOG_RENAMED = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_DESC_UPDATED = "updated description";
    public static final String LOG_TYPE_CHANGED = "changed type to <strong>%s</strong>";
    public static final String LOG_PRIORITY_CHANGED = "changed priority to <strong>%s</strong>";
    public static final String LOG_POINTS_CHANGED = "changed points to <strong>%d</strong>";
    public static final String LOG_ESTIMATE_CHANGED = "changed estimate to <strong>%s</strong>h";
    public static final String LOG_START_DATE_CHANGED = "changed start date";
    public static final String LOG_DUE_DATE_CHANGED = "changed due date";
    public static final String LOG_STATUS_CHANGED = "changed status from <strong>%s</strong> to <strong>%s</strong>";
    public static final String LOG_REORDERED = "reordered in <strong>%s</strong> column";
    public static final String LOG_MOVED_BACKLOG = "moved to <strong>Backlog</strong>";
    public static final String LOG_MOVED_SPRINT = "moved to sprint <strong>%s</strong>";
    public static final String LOG_REMOVED_EPIC = "removed from Epic";
    public static final String LOG_ADDED_EPIC = "added to epic <strong>%s</strong>";
    public static final String LOG_CHANGED_EPIC = "changed epic from <strong>%s</strong> to <strong>%s</strong>";
    public static final String LOG_UNASSIGNED = "unassigned";
    public static final String LOG_ASSIGNED = "assigned to <strong>%s</strong>";

    public static final String LABEL_NONE = "None";

    // Khai bao cac hang so cho Excel Import/Export
    public static final String EXCEL_SHEET_NAME = "Tasks Import";
    public static final String[] EXCEL_COLUMNS = {"Title", "Description", "Assignee Email", "Priority", "Status", "Start Date", "Due Date", "Story Points", "Estimated Hours"};
    public static final String[] EXCEL_DROPDOWN_PRIORITY = {"LOW", "MEDIUM", "HIGH", "URGENT"};
    public static final String[] EXCEL_DROPDOWN_STATUS = {"To Do", "In Progress", "Done", "Backlog"};
    public static final String EXCEL_FILE_EXTENSION_XLSX = ".xlsx";
    public static final String EXCEL_FILE_EXTENSION_XLS = ".xls";
    
    public static final int EXCEL_COL_WIDTH_MULTIPLIER = 256;
    public static final int EXCEL_WIDTH_DESC = 50;
    public static final int EXCEL_WIDTH_DATE = 20;
    public static final int EXCEL_WIDTH_DEFAULT = 25;
    
    public static final String VALIDATION_TITLE_REQUIRED = "Title is required";
    public static final String VALIDATION_USER_NOT_IN_PROJECT = "User '%s' is not in project";
    public static final String VALIDATION_INVALID_START_DATE = "Invalid Start Date";
    public static final String VALIDATION_INVALID_DUE_DATE = "Invalid Due Date";
    public static final String VALIDATION_DATE_ORDER = "Start Date must be before Due Date";
    public static final String VALIDATION_INVALID_PRIORITY = "Invalid Priority";
    public static final String VALIDATION_POINTS_NAN = "Points must be number";
    public static final String VALIDATION_HOURS_NAN = "Hours must be number";

    // Khai bao cac bien phu thuoc
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final EpicRepository epicRepository;
    private final ProjectStatusRepository projectStatusRepository;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
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

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_TASK, description = DESC_DRAG_DROP)
    public TaskResponse updateTaskSprint(Integer taskId, Integer newSprintId, Integer newSortOrder) {
        // Tim kiem cong viec
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND));

        Integer projectId = task.getProject().getId();

        // Xac dinh Sprint dich hoac dua ve Backlog
        Sprint targetSprint = null;
        if (newSprintId != null) {
            targetSprint = sprintRepository.findById(newSprintId)
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));
            
            if (!targetSprint.getProject().getId().equals(projectId)) {
                throw new BadRequestException(ERROR_PROJECT_MISMATCH_SPRINT);
            }
        }

        // Xu ly vi tri sap xep
        if (newSortOrder == null) {
            // Day xuong cuoi cung neu khong chi dinh vi tri
            if (targetSprint != null) {
                newSortOrder = taskRepository.findMaxSortOrderBySprintId(newSprintId) + 1;
            } else {
                newSortOrder = taskRepository.findMaxSortOrderByProjectIdAndSprintIsNull(projectId) + 1;
            }
        } else {
            // Day cac cong viec khac lui xuong de chen vao
            if (targetSprint != null) {
                taskRepository.shiftSortOrderInSprint(newSprintId, newSortOrder);
            } else {
                taskRepository.shiftSortOrderInBacklog(projectId, newSortOrder);
            }
        }

        task.setSprint(targetSprint);
        task.setSortOrder(newSortOrder);

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_TASK, description = DESC_CREATE_TASK)
    public TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request) {
        // Lay thong tin nguoi tao (Nguoi dang dang nhap)
        User creator = securityService.getCurrentAuthenticatedUser();

        // Kiem tra du an ton tai
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));

        // Tim trang thai cot mac dinh (cot dau tien tren Board) de gan vao cong viec moi
        ProjectStatus defaultStatus = projectStatusRepository.findFirstByProject_IdOrderBySortOrderAsc(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_NO_STATUS_BOARD));

        // Xu ly chuyen vao Sprint neu request co truyen sprintId
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND_ID + request.getSprintId()));

            // Kiem tra xem Sprint co thuoc ve Du an nay khong
            if (!sprint.getProject().getId().equals(projectId)) {
                throw new BadRequestException(ERROR_PROJECT_MISMATCH_SPRINT);
            }
        }

        // Xu ly Epic (Hang muc lon)
        Epic epic = null;
        if (request.getEpicId() != null) {
            epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND));
        }
        
        // Xu ly Nguoi thuc hien (Assignee)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_ASSIGNEE_NOT_FOUND));
        }

        // ==============================================================
        // LOGIC SINH MÃ VÀ SẮP XẾP MỚI (CHỐNG LỖI TRÙNG LẶP KHI XÓA TASK)
        // ==============================================================

        // 1. Sinh ma tu dong (Task Code): Lay so lon nhat hien tai + 1
        Integer maxSequence = taskRepository.findMaxTaskSequenceByProjectId(projectId);
        int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;
        String newCode = project.getProjectCode() + "-" + nextSequence;

        // 2. Tinh toan thu tu sap xep (Sort Order): Dat Task moi xuong cuoi cung cua cot trang thai
        Integer maxSortOrder = taskRepository.findMaxSortOrderByStatusId(projectId, defaultStatus.getId());
        int nextSortOrder = (maxSortOrder != null ? maxSortOrder : 0) + 1;

        // ==============================================================

        // Khoi tao cong viec moi voi cac gia tri da tinh toan
        Task newTask = Task.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskCode(newCode) // Su dung ma an toan moi sinh ra
                .taskType(request.getTaskType() != null ? request.getTaskType() : TaskType.TASK)
                .status(defaultStatus)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .sprint(sprint) 
                .epic(epic)
                .assignee(assignee)
                .assigner(creator)
                .createdBy(creator)
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .sortOrder(nextSortOrder) // Dat the xuong cuoi cung cot
                .build();

        // Luu vao database
        Task savedTask = taskRepository.save(newTask);
        
        // Map sang Response va tra ve
        return mapToTaskSummaryResponse(savedTask);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_MOVE_STATUS, entityType = ENTITY_TASK, description = DESC_MOVE_STATUS)
    public TaskResponse moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request) {
        // Kiem tra cong viec ton tai
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));

        Integer projectId = task.getProject().getId();
        Integer newStatusId = request.getNewStatusId();

        // Kiem tra trang thai moi ton tai va hop le
        ProjectStatus newStatus = projectStatusRepository.findById(newStatusId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_STATUS_NOT_FOUND));

        if (!newStatus.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_PROJECT_MISMATCH_STATUS);
        }

        // Tinh toan lai vi tri cua cac cong viec khac trong cot dich
        Integer newSortOrder = request.getNewSortOrder();
        if (newSortOrder == null) {
            newSortOrder = taskRepository.findMaxSortOrderByStatusId(projectId, newStatusId) + 1;
        } else {
            taskRepository.shiftSortOrderInStatus(projectId, newStatusId, newSortOrder);
        }

        String oldStatusName = (task.getStatus() != null) ? task.getStatus().getName() : LABEL_NONE;
        String newStatusName = newStatus.getName();

        // Ghi log chi tiet viec dich chuyen trang thai hoac thay doi vi tri
        if (!oldStatusName.equals(newStatusName)) {
             ActivityLogContext.setDetail(String.format(LOG_STATUS_CHANGED, oldStatusName, newStatusName));
        } else {
             ActivityLogContext.setDetail(String.format(LOG_REORDERED, newStatusName));
        }

        task.setStatus(newStatus);
        task.setSortOrder(newSortOrder);

        // Cap nhat thoi gian hoan thanh dua tren tinh chat cua cot trang thai
        if (newStatus.getIsCompletedStatus()) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_TASK, description = DESC_UPDATE_TASK)
    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));
        
        Integer projectId = task.getProject().getId();
        StringBuilder changes = new StringBuilder();

        // Cap nhat tieu de
        if (request.getTitle() != null && !request.getTitle().isBlank() 
                && !request.getTitle().equals(task.getTitle())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_RENAMED, task.getTitle(), request.getTitle()));
            task.setTitle(request.getTitle());
        }

        // Cap nhat mo ta
        if (request.getDescription() != null && !request.getDescription().equals(task.getDescription())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_DESC_UPDATED);
            task.setDescription(request.getDescription());
        }

        // Cap nhat loai cong viec
        if (request.getTaskType() != null && request.getTaskType() != task.getTaskType()) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_TYPE_CHANGED, request.getTaskType()));
            task.setTaskType(request.getTaskType());
        }

        // Cap nhat do uu tien
        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_PRIORITY_CHANGED, request.getPriority()));
            task.setPriority(request.getPriority());
        }

        // Cap nhat khoi luong cong viec (Point)
        if (request.getStoryPoints() != null && !Objects.equals(request.getStoryPoints(), task.getStoryPoints())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_POINTS_CHANGED, request.getStoryPoints()));
            task.setStoryPoints(request.getStoryPoints());
        }

        // Cap nhat thoi gian du kien
        if (request.getEstimatedHours() != null) {
            boolean isDifferent;
            if (task.getEstimatedHours() == null) {
                isDifferent = true;
            } else {
                isDifferent = request.getEstimatedHours().compareTo(task.getEstimatedHours()) != 0;
            }

            if (isDifferent) {
                if (changes.length() > 0) {
                    changes.append(", ");
                }
                changes.append(String.format(LOG_ESTIMATE_CHANGED, request.getEstimatedHours()));
                task.setEstimatedHours(request.getEstimatedHours());
            }
        }

        // Cap nhat ngay bat dau
        if (request.getStartDate() != null) {
            boolean isDifferent = (task.getStartDate() == null) || !request.getStartDate().isEqual(task.getStartDate());
            if (isDifferent) {
                if (changes.length() > 0) {
                    changes.append(", ");
                }
                changes.append(LOG_START_DATE_CHANGED);
                task.setStartDate(request.getStartDate());
            }
        }

        // Cap nhat ngay dao han
        if (request.getDueDate() != null) {
            boolean isDifferent = (task.getDueDate() == null) || !request.getDueDate().isEqual(task.getDueDate());
            if (isDifferent) {
                if (changes.length() > 0) {
                    changes.append(", ");
                }
                changes.append(LOG_DUE_DATE_CHANGED);
                task.setDueDate(request.getDueDate());
            }
        }

        // Cap nhat trang thai
        if (request.getStatusId() != null) {
            Integer oldStatusId = task.getStatus() != null ? task.getStatus().getId() : 0;
            if (!request.getStatusId().equals(oldStatusId)) {
                ProjectStatus newStatus = projectStatusRepository.findById(request.getStatusId())
                        .orElseThrow(() -> new ResourceNotFoundException(ERROR_STATUS_NOT_FOUND));
                
                if (!newStatus.getProject().getId().equals(projectId)) {
                    throw new BadRequestException(ERROR_PROJECT_MISMATCH_STATUS);
                }

                if (changes.length() > 0) {
                    changes.append(", ");
                }
                String oldName = task.getStatus() != null ? task.getStatus().getName() : LABEL_NONE;
                changes.append(String.format(LOG_STATUS_CHANGED, oldName, newStatus.getName()));
                
                task.setStatus(newStatus);
            }
        }

        // Cap nhat Sprint
        if (request.getSprintId() != null) {
            Integer oldSprintId = task.getSprint() != null ? task.getSprint().getId() : 0;
            
            if (!request.getSprintId().equals(oldSprintId)) {
                if (request.getSprintId() == 0) {
                    if (changes.length() > 0) {
                        changes.append(", ");
                    }
                    changes.append(LOG_MOVED_BACKLOG);
                    task.setSprint(null);
                } else {
                    Sprint sprint = sprintRepository.findById(request.getSprintId())
                            .orElseThrow(() -> new ResourceNotFoundException(ERROR_SPRINT_NOT_FOUND));
                            
                    if (!sprint.getProject().getId().equals(projectId)) {
                        throw new BadRequestException(ERROR_PROJECT_MISMATCH_SPRINT);
                    }

                    if (changes.length() > 0) {
                        changes.append(", ");
                    }
                    changes.append(String.format(LOG_MOVED_SPRINT, sprint.getName()));
                    task.setSprint(sprint);
                }
            }
        }

        // Cap nhat Epic
        if (request.getEpicId() != null) {
             Integer oldEpicId = task.getEpic() != null ? task.getEpic().getId() : 0;
             
             if (!request.getEpicId().equals(oldEpicId)) {
                 if (request.getEpicId() == 0) {
                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(LOG_REMOVED_EPIC);
                     task.setEpic(null);
                 } else {
                     Epic epic = epicRepository.findById(request.getEpicId())
                             .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND));
                             
                     if (!epic.getProject().getId().equals(projectId)) {
                         throw new BadRequestException(ERROR_PROJECT_MISMATCH_EPIC);
                     }

                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(String.format(LOG_ADDED_EPIC, epic.getName()));
                     task.setEpic(epic);
                 }
             }
        }

        // Cap nhat nguoi thuc hien
        if (request.getAssigneeId() != null) {
             Integer oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : 0;
             
             if (!request.getAssigneeId().equals(oldAssigneeId)) {
                 if (request.getAssigneeId() == 0) {
                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(LOG_UNASSIGNED);
                     task.setAssignee(null);
                 } else {
                     User assignee = userRepository.findById(request.getAssigneeId())
                             .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));
                     
                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(String.format(LOG_ASSIGNED, assignee.getFullName()));
                     task.setAssignee(assignee);
                 }
             }
        }

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_TASK, description = DESC_UPDATE_EPIC)
    public TaskResponse updateTaskEpic(Integer taskId, UpdateTaskEpicRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));

        Integer newEpicId = request.getEpicId();
        
        String oldEpicName = task.getEpic() != null ? task.getEpic().getName() : LABEL_NONE;
        String newEpicName = LABEL_NONE;

        if (newEpicId == null || newEpicId == 0) {
            task.setEpic(null);
            ActivityLogContext.setDetail(String.format("removed from epic <strong>%s</strong>", oldEpicName));

        } else {
            Epic epic = epicRepository.findById(newEpicId)
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_EPIC_NOT_FOUND));

            if (!task.getProject().getId().equals(epic.getProject().getId())) {
                throw new BadRequestException(ERROR_PROJECT_MISMATCH_EPIC);
            }
            
            newEpicName = epic.getName();
            task.setEpic(epic);
            
            ActivityLogContext.setDetail(String.format(LOG_CHANGED_EPIC, oldEpicName, newEpicName));
        }

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskDetails(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));

        return mapToTaskResponse(task);
    }
    
    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_TASK, description = DESC_DELETE_TASK)
    public TaskResponse deleteTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));

        TaskResponse response = mapToTaskResponse(task);

        // Xoa cac binh luan thu cong de tranh xung dot tu phia Hibernate truoc khi xoa cong viec chinh
        if (task.getComments() != null && !task.getComments().isEmpty()) {
             taskCommentRepository.deleteAll(task.getComments());
             task.setComments(new ArrayList<>()); 
        }

        taskRepository.delete(task);

        return response;
    }
    
    @Override
    public byte[] generateImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(EXCEL_SHEET_NAME);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < EXCEL_COLUMNS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXCEL_COLUMNS[i]);
                cell.setCellStyle(headerStyle);
                
                // Thiet lap do rong cho cac cot theo quy chuan
                if (i == 1) {
                    sheet.setColumnWidth(i, EXCEL_WIDTH_DESC * EXCEL_COL_WIDTH_MULTIPLIER); 
                } else if (i == 5 || i == 6) {
                    sheet.setColumnWidth(i, EXCEL_WIDTH_DATE * EXCEL_COL_WIDTH_MULTIPLIER); 
                } else {
                    sheet.setColumnWidth(i, EXCEL_WIDTH_DEFAULT * EXCEL_COL_WIDTH_MULTIPLIER);
                }
            }

            // Tao du lieu mau
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Fix Login Bug");
            row.createCell(1).setCellValue("Fix error 401 on login");
            row.createCell(2).setCellValue("dev1@techvision.com");
            row.createCell(3).setCellValue("HIGH");
            row.createCell(4).setCellValue("To Do");
            row.createCell(5).setCellValue("2025-12-01"); 
            row.createCell(6).setCellValue("2025-12-31");
            row.createCell(7).setCellValue(5);
            row.createCell(8).setCellValue(8.0);
            
            // Xy ly dropdown list trong Excel
            DataValidationHelper helper = sheet.getDataValidationHelper();
            
            DataValidationConstraint priorityConstraint = helper.createExplicitListConstraint(EXCEL_DROPDOWN_PRIORITY);
            sheet.addValidationData(helper.createValidation(priorityConstraint, new CellRangeAddressList(1, 1000, 3, 3)));
            
            DataValidationConstraint statusConstraint = helper.createExplicitListConstraint(EXCEL_DROPDOWN_STATUS);
            sheet.addValidationData(helper.createValidation(statusConstraint, new CellRangeAddressList(1, 1000, 4, 4)));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException(ERROR_GENERATE_TEMPLATE + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskImportPreviewResponse> previewImportTasks(Integer projectId, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(EXCEL_FILE_EXTENSION_XLSX) && !fileName.endsWith(EXCEL_FILE_EXTENSION_XLS))) {
             throw new BadRequestException(ERROR_INVALID_EXCEL);
        }

        Map<String, User> projectMembersMap = projectMemberRepository.findByProject_Id(projectId, Pageable.unpaged())
                .stream()
                .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                .map(ProjectMember::getUser)
                .collect(Collectors.toMap(u -> u.getEmail().toLowerCase(), u -> u));

        List<TaskImportPreviewResponse> previewList = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter(); 

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 0;

            for (Row row : sheet) {
                if (rowIndex == 0) { 
                    rowIndex++; 
                    continue; 
                }

                String title = getCellValue(row.getCell(0), dataFormatter);
                if (title.isEmpty()) {
                    break; 
                }

                String description = getCellValue(row.getCell(1), dataFormatter);
                String assigneeEmail = getCellValue(row.getCell(2), dataFormatter);
                String priority = getCellValue(row.getCell(3), dataFormatter);
                String status = getCellValue(row.getCell(4), dataFormatter);
                
                LocalDateTime startDt = parseExcelDate(row.getCell(5));
                String startDateStr = getCellValue(row.getCell(5), dataFormatter);
                if (startDt == null && !startDateStr.isEmpty()) {
                    startDt = parseDateFlexible(startDateStr);
                }

                LocalDateTime dueDt = parseExcelDate(row.getCell(6));
                String dueDateStr = getCellValue(row.getCell(6), dataFormatter);
                if (dueDt == null && !dueDateStr.isEmpty()) {
                    dueDt = parseDateFlexible(dueDateStr);
                }

                String pointsStr = getCellValue(row.getCell(7), dataFormatter);
                String hoursStr = getCellValue(row.getCell(8), dataFormatter);

                // Kiem tra du lieu dau vao
                List<String> errors = new ArrayList<>();

                if (title.isEmpty()) {
                    errors.add(VALIDATION_TITLE_REQUIRED);
                }

                if (!assigneeEmail.isEmpty()) {
                    if (!projectMembersMap.containsKey(assigneeEmail.toLowerCase())) {
                        errors.add(String.format(VALIDATION_USER_NOT_IN_PROJECT, assigneeEmail));
                    }
                }

                if (!startDateStr.isEmpty() && startDt == null) {
                    errors.add(VALIDATION_INVALID_START_DATE);
                }
                
                if (!dueDateStr.isEmpty() && dueDt == null) {
                    errors.add(VALIDATION_INVALID_DUE_DATE);
                }

                if (startDt != null && dueDt != null && startDt.isAfter(dueDt)) {
                    errors.add(VALIDATION_DATE_ORDER);
                }
                
                if (!priority.isEmpty()) {
                    try { 
                        TaskPriority.valueOf(priority.toUpperCase()); 
                    } catch (Exception e) { 
                        errors.add(VALIDATION_INVALID_PRIORITY); 
                    }
                }

                Integer points = null;
                try { 
                    if (!pointsStr.isEmpty()) {
                        points = (int) Double.parseDouble(pointsStr); 
                    }
                } catch(Exception e) { 
                    errors.add(VALIDATION_POINTS_NAN); 
                }
                
                Double hours = null;
                try { 
                    if (!hoursStr.isEmpty()) {
                        hours = Double.parseDouble(hoursStr); 
                    }
                } catch(Exception e) { 
                    errors.add(VALIDATION_HOURS_NAN); 
                }

                String startDisplay = startDt != null ? startDt.toLocalDate().toString() : startDateStr;
                String dueDisplay = dueDt != null ? dueDt.toLocalDate().toString() : dueDateStr;

                previewList.add(TaskImportPreviewResponse.builder()
                        .rowIndex(rowIndex + 1)
                        .title(title)
                        .description(description)
                        .assigneeEmail(assigneeEmail)
                        .priority(priority)
                        .statusName(status)
                        .startDate(startDisplay)
                        .dueDate(dueDisplay)
                        .storyPoints(points)
                        .estimatedHours(hours)
                        .isValid(errors.isEmpty())
                        .errors(errors)
                        .build());
                rowIndex++;
            }
        } catch (Exception e) {
            throw new BadRequestException(ERROR_READING_EXCEL + e.getMessage());
        }
        return previewList;
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_IMPORT, entityType = ENTITY_TASK, description = DESC_IMPORT_TASK)
    public ImportTaskResultResponse saveImportedTasks(Integer projectId, List<TaskImportPreviewResponse> rows) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));
                
        Map<String, User> memberMap = projectMemberRepository.findByProject_Id(projectId, Pageable.unpaged())
                .stream()
                .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                .map(ProjectMember::getUser)
                .collect(Collectors.toMap(u -> u.getEmail().toLowerCase(), u -> u));
        
        Map<String, ProjectStatus> statusMap = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId)
                .stream()
                .collect(Collectors.toMap(s -> s.getName().toLowerCase(), s -> s));
                
        ProjectStatus defaultStatus = statusMap.values().stream()
                .min(Comparator.comparingInt(ProjectStatus::getSortOrder))
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_STATUS_NOT_FOUND));

        User creator = securityService.getCurrentAuthenticatedUser();
        long currentSort = taskRepository.countByProjectId(projectId);
        List<Task> tasksToSave = new ArrayList<>();

        for (TaskImportPreviewResponse row : rows) {
            if (row.getTitle() == null || row.getTitle().trim().isEmpty()) {
                continue; 
            }

            User assignee = null;
            if (row.getAssigneeEmail() != null && !row.getAssigneeEmail().isBlank()) {
                assignee = memberMap.get(row.getAssigneeEmail().trim().toLowerCase());
            }

            ProjectStatus status = defaultStatus;
            if (row.getStatusName() != null && statusMap.containsKey(row.getStatusName().trim().toLowerCase())) {
                status = statusMap.get(row.getStatusName().trim().toLowerCase());
            }

            TaskPriority priority = TaskPriority.MEDIUM;
            try {
                if (row.getPriority() != null) {
                    priority = TaskPriority.valueOf(row.getPriority().toUpperCase());
                }
            } catch (Exception ignored) {
            }

            LocalDateTime startDate = null;
            try {
                if (row.getStartDate() != null && !row.getStartDate().isBlank()) {
                    startDate = LocalDate.parse(row.getStartDate()).atStartOfDay();
                }
            } catch (Exception ignored) {
            }

            LocalDateTime dueDate = null;
            try {
                if (row.getDueDate() != null && !row.getDueDate().isBlank()) {
                    dueDate = LocalDate.parse(row.getDueDate()).atStartOfDay();
                }
            } catch (Exception ignored) {
            }

            Task task = Task.builder()
                    .project(project)
                    .taskCode(project.getProjectCode() + "-" + (++currentSort))
                    .title(row.getTitle())
                    .description(row.getDescription())
                    .assignee(assignee) 
                    .status(status)     
                    .priority(priority) 
                    .taskType(TaskType.TASK)
                    .startDate(startDate)
                    .dueDate(dueDate)   
                    .storyPoints(row.getStoryPoints())
                    .estimatedHours(row.getEstimatedHours() != null ? BigDecimal.valueOf(row.getEstimatedHours()) : null)
                    .sortOrder((int)currentSort)
                    .createdBy(creator)
                    .assigner(creator)
                    .build();
            
            tasksToSave.add(task);
        }

        taskRepository.saveAll(tasksToSave);
        
        return ImportTaskResultResponse.builder()
                .successCount(tasksToSave.size())
                .totalRows(rows.size())
                .errorCount(0)
                .errors(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public void archiveTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));
        
        task.setIsArchived(true);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void restoreTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND_ID + taskId));
        
        task.setIsArchived(false);
        taskRepository.save(task);
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private String getCellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        String val = formatter.formatCellValue(cell);
        return val.replace("\u00A0", " ").trim();
    }

    private LocalDateTime parseExcelDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception e) { 
            return null; 
        }
        return null;
    }
        
    private LocalDateTime parseDateFlexible(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd", "M/d/yyyy", "MM/dd/yyyy", "d/M/yyyy", "dd/MM/yyyy"};
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            } catch (Exception e) {
                // Thu dinh dang tiep theo
            }
        }
        return null;
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

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

        int totalSubtasks = (task.getSubTasks() != null) ? task.getSubTasks().size() : 0;
        int completedSubtasks = (task.getSubTasks() != null) ?
                (int) task.getSubTasks().stream().filter(st -> st.getStatus() == SubTaskStatus.DONE).count() : 0;

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

        return TaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .workspaceId(task.getProject().getWorkspace().getId())
                .companyId(task.getProject().getWorkspace().getCompany().getId())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
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
                .tags(tagInfos)
                .subtaskSummary(TaskResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())
                .storyPoints(task.getStoryPoints())
                .estimatedHours(task.getEstimatedHours())
                .loggedHours(task.getLoggedHours())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .createdById(createdBy != null ? createdBy.getId() : null)
                .createdByName(createdBy != null ? createdBy.getFullName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        ProjectStatus status = task.getStatus();

        int totalSubtasks = 0;
        int completedSubtasks = 0;
        if (task.getSubTasks() != null) {
            totalSubtasks = task.getSubTasks().size();
            completedSubtasks = (int) task.getSubTasks().stream()
                    .filter(st -> st.getStatus() == SubTaskStatus.DONE)
                    .count();
        }

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