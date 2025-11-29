package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.*;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.repository.SubTaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SubTaskService;
import com.quanlyduan.project_manager_api.util.ProjectHierarchyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Sử dụng Lombok để tự sinh Constructor (Xóa constructor thủ công đi cho gọn)
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final UserRepository userRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    // --- CREATE (Giữ nguyên theo ý bạn) ---
    @Override
    @Transactional
    public SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request) {
        Task parentTask = validator.validateTask(companyId, workspaceId, projectId, taskId);
        User creator = securityService.getCurrentAuthenticatedUser();
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }
        Integer nextSortOrder = subTaskRepository.countByParentTask_Id(taskId);
        SubTask subTask = SubTask.builder().parentTask(parentTask).title(request.getTitle()).description(request.getDescription())
                .status(SubTaskStatus.TO_DO).assignee(assignee).estimatedHours(request.getEstimatedHours()).sortOrder(nextSortOrder).createdBy(creator).build();
        SubTask saved = subTaskRepository.save(subTask);
        return mapToResponse(saved);
    }

    // --- API 1: Xem chi tiết từng SubTask (OK) ---
    @Override
    @Transactional(readOnly = true)
    public SubTaskResponse getSubTaskById(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // Sử dụng Validator để đảm bảo SubTask tồn tại và thuộc đúng cấu trúc Project/Task cha
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        return mapToResponse(subTask);
    }

    // --- API 2: Xem toàn bộ SubTask kèm chi tiết (ĐÃ SỬA LỖI) ---
    @Override
    @Transactional(readOnly = true)
    public List<SubTaskResponse> getAllSubTasksByTaskId(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        // Kiểm tra Task cha có hợp lệ không
        validator.validateTask(companyId, workspaceId, projectId, taskId);

        // [FIX] Phải gọi hàm 'findBy...' để lấy danh sách, không dùng 'countBy...'
        List<SubTask> subTasks = subTaskRepository.findByParentTask_IdOrderBySortOrderAscCreatedAtAsc(taskId);

        return subTasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- UPDATE (Giữ nguyên theo ý bạn) ---
    @Override
    @Transactional
    public SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request) {
        SubTask subTask = validator.validateSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        if (request.getTitle() != null) subTask.setTitle(request.getTitle());
        if (request.getDescription() != null) subTask.setDescription(request.getDescription());
        if (request.getStatus() != null) subTask.setStatus(request.getStatus());
        if (request.getEstimatedHours() != null) subTask.setEstimatedHours(request.getEstimatedHours());
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            subTask.setAssignee(assignee);
        }
        SubTask saved = subTaskRepository.save(subTask);
        return mapToResponse(saved);
    }

    // --- MAPPER ---
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

                // Map 2 trường này để API GET trả về đủ thông tin (nếu DB có dữ liệu)
                .startDate(subTask.getStartDate())
                .dueDate(subTask.getDueDate())
                .build();
    }
}