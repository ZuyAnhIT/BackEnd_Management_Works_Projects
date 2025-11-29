package com.quanlyduan.project_manager_api.service.impl;

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
import com.quanlyduan.project_manager_api.util.ProjectHierarchyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository; 
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    public SubTaskServiceImpl(SubTaskRepository subTaskRepository, UserRepository userRepository, TaskRepository taskRepository, ProjectHierarchyValidator validator, SecurityService securityService) {
        this.subTaskRepository = subTaskRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubTaskResponse> getSubTasks(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        validator.validateTask(companyId, workspaceId, projectId, taskId);
        return subTaskRepository.findByParentTask_IdOrderBySortOrderAsc(taskId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubTaskResponse getSubTaskDetail(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        return mapToResponse(subTask);
    }

    @Override
    @Transactional
    public SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request) {
        Task parentTask = validator.validateTask(companyId, workspaceId, projectId, taskId);
        validator.validateProjectMember(projectId, request.getAssigneeId());
        User creator = securityService.getCurrentAuthenticatedUser();
        
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }
        
        Integer nextSortOrder = subTaskRepository.countByParentTask_Id(taskId);
        
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
        
        // Khi save(), @CreationTimestamp trong Entity sẽ tự động điền createdAt
        SubTask saved = subTaskRepository.save(subTask);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request) {
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        
        if (request.getTitle() != null) subTask.setTitle(request.getTitle());
        if (request.getDescription() != null) subTask.setDescription(request.getDescription());
        if (request.getStatus() != null) subTask.setStatus(request.getStatus());
        if (request.getEstimatedHours() != null) subTask.setEstimatedHours(request.getEstimatedHours());
        
        if (request.getAssigneeId() != null) {
            // 2. Validate Assignee (MỚI THÊM)
            // Nếu người dùng gửi assigneeId mới lên, phải kiểm tra xem user đó có thuộc project không
            validator.validateProjectMember(projectId, request.getAssigneeId());

            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            subTask.setAssignee(assignee);
        }else {
            // Logic tùy chọn: Nếu gửi assigneeId là null thì có gỡ người làm không? 
            // Nếu muốn gỡ thì: subTask.setAssignee(null);
        }
        
        // Khi save(), @UpdateTimestamp sẽ tự động cập nhật updatedAt
        SubTask saved = subTaskRepository.save(subTask);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        subTaskRepository.delete(subTask);
    }

    // --- Helper Mapping ---
    private SubTaskResponse mapToResponse(SubTask subTask) {
        return SubTaskResponse.builder()
                .id(subTask.getId())
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