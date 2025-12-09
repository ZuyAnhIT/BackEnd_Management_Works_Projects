// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/SubTaskServiceImpl.java
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

    private final SubTaskRepository subTaskRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public SubTaskServiceImpl(SubTaskRepository subTaskRepository, UserRepository userRepository, TaskRepository taskRepository, ProjectHierarchyValidator validator, SecurityService securityService) {
        this.subTaskRepository = subTaskRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH SUBTASK (GET SUBTASKS)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<SubTaskResponse> getSubTasks(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        // 1. Validate: Kiểm tra tính hợp lệ của Task (Hierarchy)
        validator.validateTask(companyId, workspaceId, projectId, taskId);
        
        // 2. Lấy danh sách SubTask theo Task ID, sắp xếp theo thứ tự
        return subTaskRepository.findByParentTask_IdOrderBySortOrderAsc(taskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // 2. XEM CHI TIẾT SUBTASK (GET SUBTASK DETAIL)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public SubTaskResponse getSubTaskDetail(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // 1. Validate: Kiểm tra tính hợp lệ của SubTask (Hierarchy)
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        
        // 2. Map và trả về
        return mapToResponse(subTask);
    }

    // ======================================================
    // 3. TẠO SUBTASK MỚI (CREATE SUBTASK)
    // ======================================================
    @LogActivity(action = "CREATE", entityType = "SUBTASK", description = "Create new Subtask")
    @Override
    @Transactional
    public SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request) {
        // 1. Validate Task và Lấy Task cha
        Task parentTask = validator.validateTask(companyId, workspaceId, projectId, taskId);
        
        // 2. Validate Assignee: Kiểm tra người được giao có thuộc Project không
        validator.validateProjectMember(projectId, request.getAssigneeId());
        
        // 3. Lấy thông tin người tạo hiện tại
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // 4. Tìm Assignee
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }
        
        // 5. Tính toán vị trí (sortOrder)
        // SortOrder mới sẽ là số lượng subtask hiện tại (bắt đầu từ 0)
        Integer nextSortOrder = subTaskRepository.countByParentTask_Id(taskId);
        
        // 6. Tạo Entity
        SubTask subTask = SubTask.builder()
                .parentTask(parentTask)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(SubTaskStatus.TO_DO) // Mặc định là TO_DO
                .assignee(assignee)
                .estimatedHours(request.getEstimatedHours())
                .sortOrder(nextSortOrder)
                .createdBy(creator)
                .build();
        
        // 7. Lưu và trả về
        SubTask saved = subTaskRepository.save(subTask);
        
        return mapToResponse(saved);
    }

    // ======================================================
    // 4. CẬP NHẬT SUBTASK (UPDATE SUBTASK)
    // ======================================================
   @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "SUBTASK", description = "Update Subtask")
    public SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request) {
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);

        StringBuilder changes = new StringBuilder();

        // 1. Title
        if (request.getTitle() != null && !request.getTitle().equals(subTask.getTitle())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", subTask.getTitle(), request.getTitle()));
            subTask.setTitle(request.getTitle());
        }

        // 2. Description
        if (request.getDescription() != null && !request.getDescription().equals(subTask.getDescription())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append("updated description");
             subTask.setDescription(request.getDescription());
        }

        // 3. Status
        if (request.getStatus() != null && request.getStatus() != subTask.getStatus()) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed status from <strong>%s</strong> to <strong>%s</strong>", subTask.getStatus(), request.getStatus()));
            subTask.setStatus(request.getStatus());
        }

        // 4. Estimate
        if (request.getEstimatedHours() != null && !request.getEstimatedHours().equals(subTask.getEstimatedHours())) {
             subTask.setEstimatedHours(request.getEstimatedHours());
        }
        
        // 5. Assignee
        if (request.getAssigneeId() != null) {
            Integer oldAssigneeId = subTask.getAssignee() != null ? subTask.getAssignee().getId() : 0;
            if (!request.getAssigneeId().equals(oldAssigneeId)) {
                if (request.getAssigneeId() == 0) {
                     if (changes.length() > 0) changes.append(", ");
                     changes.append("unassigned");
                     subTask.setAssignee(null);
                } else {
                    validator.validateProjectMember(projectId, request.getAssigneeId());
                    User assignee = userRepository.findById(request.getAssigneeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
                    
                    if (changes.length() > 0) changes.append(", ");
                    changes.append(String.format("assigned to <strong>%s</strong>", assignee.getFullName()));
                    subTask.setAssignee(assignee);
                }
            }
        }

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
            //  ActivityLogContext.setDetail("updated details");
        }

        SubTask saved = subTaskRepository.save(subTask);
        return mapToResponse(saved);
    }

    // ======================================================
    // 5. XÓA SUBTASK (DELETE SUBTASK)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "SUBTASK", description = "Delete Subtask")
    public void deleteSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // 1. Validate SubTask và Lấy Entity (Đảm bảo SubTask thuộc đúng Hierarchy)
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        
        // 2. Thực hiện xóa cứng
        subTaskRepository.delete(subTask);
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER: MAPPER
    // ======================================================
    /**
     * Helper: Map SubTask Entity sang SubTaskResponse DTO.
     */
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
                
                // Mapping Audit Fields
                .createdById(subTask.getCreatedBy() != null ? subTask.getCreatedBy().getId() : null)
                .createdByName(subTask.getCreatedBy() != null ? subTask.getCreatedBy().getFullName() : null)
                .createdAt(subTask.getCreatedAt())
                .updatedAt(subTask.getUpdatedAt())
                .build();
    }
}