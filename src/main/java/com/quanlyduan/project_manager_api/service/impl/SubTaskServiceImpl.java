package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.SubTask;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.repository.SubTaskRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SubTaskService;
import com.quanlyduan.project_manager_api.validation.ProjectHierarchyValidator;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    // Khai bao hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    public static final String ENTITY_SUBTASK = "SUBTASK";

    public static final String DESC_CREATE_SUBTASK = "Create new Subtask";
    public static final String DESC_UPDATE_SUBTASK = "Update Subtask";
    public static final String DESC_DELETE_SUBTASK = "Delete Subtask";

    public static final String ERROR_ASSIGNEE_NOT_FOUND = "Assignee not found";

    public static final String LOG_RENAMED = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_DESC_UPDATED = "updated description";
    public static final String LOG_STATUS_CHANGED = "changed status from <strong>%s</strong> to <strong>%s</strong>";
    public static final String LOG_UNASSIGNED = "unassigned";
    public static final String LOG_ASSIGNED = "assigned to <strong>%s</strong>";

    // Khai bao cac bien phu thuoc
    private final SubTaskRepository subTaskRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public SubTaskServiceImpl(SubTaskRepository subTaskRepository, 
                              UserRepository userRepository, 
                              TaskRepository taskRepository, 
                              ProjectHierarchyValidator validator, 
                              SecurityService securityService) {
        this.subTaskRepository = subTaskRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public List<SubTaskResponse> getSubTasks(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        // Kiem tra tinh hop le cua cong viec chinh theo phan cap
        validator.validateTask(companyId, workspaceId, projectId, taskId);
        
        // Lay danh sach cong viec con, sap xep theo thu tu va chuyen doi sang DTO
        return subTaskRepository.findByParentTask_IdOrderBySortOrderAsc(taskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubTaskResponse getSubTaskDetail(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // Kiem tra tinh hop le va lay chi tiet cong viec con
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        
        return mapToResponse(subTask);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_SUBTASK, description = DESC_CREATE_SUBTASK)
    public SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request) {
        // Xac thuc va lay thong tin cong viec cha
        Task parentTask = validator.validateTask(companyId, workspaceId, projectId, taskId);
        
        // Kiem tra nguoi duoc giao phai thuoc du an hien tai
        validator.validateProjectMember(projectId, request.getAssigneeId());
        
        // Lay thong tin nguoi tao hien tai
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // Lay thong tin nguoi duoc giao neu co
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_ASSIGNEE_NOT_FOUND));
        }
        
        // Tinh toan vi tri sap xep cho cong viec con moi
        Integer nextSortOrder = subTaskRepository.countByParentTask_Id(taskId);
        
        // Khoi tao thuc the cong viec con
        SubTask subTask = SubTask.builder()
                .parentTask(parentTask)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(SubTaskStatus.TO_DO)
                .assignee(assignee)
                .estimatedHours(request.getEstimatedHours())
                .sortOrder(nextSortOrder)
                .createdBy(creator)
                .build();
        
        SubTask saved = subTaskRepository.save(subTask);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_SUBTASK, description = DESC_UPDATE_SUBTASK)
    public SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request) {
        // Kiem tra va lay thong tin cong viec con can cap nhat
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);

        StringBuilder changes = new StringBuilder();

        // Cap nhat tieu de
        if (request.getTitle() != null && !request.getTitle().equals(subTask.getTitle())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_RENAMED, subTask.getTitle(), request.getTitle()));
            subTask.setTitle(request.getTitle());
        }

        // Cap nhat mo ta
        if (request.getDescription() != null && !request.getDescription().equals(subTask.getDescription())) {
             if (changes.length() > 0) {
                 changes.append(", ");
             }
             changes.append(LOG_DESC_UPDATED);
             subTask.setDescription(request.getDescription());
        }

        // Cap nhat trang thai
        if (request.getStatus() != null && request.getStatus() != subTask.getStatus()) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_STATUS_CHANGED, subTask.getStatus(), request.getStatus()));
            subTask.setStatus(request.getStatus());
        }

        // Cap nhat thoi gian uoc tinh
        if (request.getEstimatedHours() != null && !request.getEstimatedHours().equals(subTask.getEstimatedHours())) {
             subTask.setEstimatedHours(request.getEstimatedHours());
        }
        
        // Cap nhat nguoi thuc hien
        if (request.getAssigneeId() != null) {
            Integer oldAssigneeId = subTask.getAssignee() != null ? subTask.getAssignee().getId() : 0;
            if (!request.getAssigneeId().equals(oldAssigneeId)) {
                if (request.getAssigneeId() == 0) {
                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(LOG_UNASSIGNED);
                     subTask.setAssignee(null);
                } else {
                    validator.validateProjectMember(projectId, request.getAssigneeId());
                    User assignee = userRepository.findById(request.getAssigneeId())
                            .orElseThrow(() -> new ResourceNotFoundException(ERROR_ASSIGNEE_NOT_FOUND));
                    
                    if (changes.length() > 0) {
                        changes.append(", ");
                    }
                    changes.append(String.format(LOG_ASSIGNED, assignee.getFullName()));
                    subTask.setAssignee(assignee);
                }
            }
        }

        // Ghi log vao context neu co thay doi
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        SubTask saved = subTaskRepository.save(subTask);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_SUBTASK, description = DESC_DELETE_SUBTASK)
    public void deleteSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // Kiem tra hop le va lay thuc the cong viec con tu he thong phan cap
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        
        // Thuc hien xoa khoi co so du lieu
        subTaskRepository.delete(subTask);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---
    
    private SubTaskResponse mapToResponse(SubTask subTask) {
        return SubTaskResponse.builder()
                .id(subTask.getId())
                .projectId(subTask.getParentTask().getProject().getId())
                .parentTaskId(subTask.getParentTask().getId())
                .title(subTask.getTitle())
                .description(subTask.getDescription())
                .status(subTask.getStatus())
                .assigneeId(subTask.getAssignee() != null ? subTask.getAssignee().getId() : null)
                .assigneeName(subTask.getAssignee() != null ? subTask.getAssignee().getFullName() : null)
                .assigneeAvatar(subTask.getAssignee() != null ? subTask.getAssignee().getAvatarUrl() : null)
                .estimatedHours(subTask.getEstimatedHours())
                .sortOrder(subTask.getSortOrder())
                .createdById(subTask.getCreatedBy() != null ? subTask.getCreatedBy().getId() : null)
                .createdByName(subTask.getCreatedBy() != null ? subTask.getCreatedBy().getFullName() : null)
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }
}