
// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/SubTaskServiceImpl.java
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    // Constructor injection (chủ động tự tiêm, không dùng Lombok)
    public SubTaskServiceImpl(SubTaskRepository subTaskRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository,
                              SecurityService securityService) {
        this.subTaskRepository = subTaskRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional
    public SubTaskResponse createSubTask(Integer taskId, CreateSubTaskRequest request) {

        // 1. Lấy thông tin người tạo
        User creator = securityService.getCurrentAuthenticatedUser();

        // 2. Tìm Task cha
        Task parentTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy công việc với ID: " + taskId
                ));

        // 3. Xử lý Assignee (Nếu có)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy người dùng với ID: " + request.getAssigneeId()
                    ));
        }

        // 4. Tính toán Sort Order (xếp xuống cuối cùng)
        Integer currentCount = subTaskRepository.countByParentTask_Id(taskId);
        Integer newSortOrder = (currentCount != null ? currentCount : 0) + 1;

        // 5. Tạo SubTask Entity
        SubTask newSubTask = SubTask.builder()
                .parentTask(parentTask)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(SubTaskStatus.TO_DO)
                .assignee(assignee)
                .estimatedHours(request.getEstimatedHours())
                .sortOrder(newSortOrder)
                .createdBy(creator)
                .build();

        // 6. Lưu vào database
        SubTask savedSubTask = subTaskRepository.save(newSubTask);

        // 7. Map sang Response DTO và trả về
        return mapToSubTaskResponse(savedSubTask);
    }

    // =============================================
    // 2. CẬP NHẬT SUBTASK
    // =============================================
    @Override
    @Transactional
    public SubTaskResponse updateSubTask(Integer subTaskId, UpdateSubTaskRequest request) {

        // 1. Tìm SubTask
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy công việc con với ID: " + subTaskId
                ));

        // 2. Cập nhật các trường cơ bản (chỉ cập nhật nếu có giá trị mới)
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            subTask.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            subTask.setDescription(request.getDescription());
        }

        if (request.getEstimatedHours() != null) {
            subTask.setEstimatedHours(request.getEstimatedHours());
        }

        // 3. Xử lý Assignee (3 trường hợp)
        if (request.getAssigneeId() != null) {
            if (request.getAssigneeId() == 0) {
                // Trường hợp 1: assigneeId = 0 → Gỡ bỏ người được giao việc
                subTask.setAssignee(null);
            } else {
                // Trường hợp 2: assigneeId = {id} → Gán người mới
                User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy người dùng với ID: " + request.getAssigneeId()
                        ));
                subTask.setAssignee(assignee);
            }
            // Trường hợp 3: assigneeId = null → Không thay đổi (bỏ qua)
        }

        // 4. Lưu và trả về
        SubTask updatedSubTask = subTaskRepository.save(subTask);
        return mapToSubTaskResponse(updatedSubTask);
    }

    // =============================================
    // HÀM HELPER: MAP ENTITY SANG DTO (DUY NHẤT)
    // =============================================
    private SubTaskResponse mapToSubTaskResponse(SubTask subTask) {
        User assignee = subTask.getAssignee();

        return SubTaskResponse.builder()
                .id(subTask.getId())
                .parentTaskId(subTask.getParentTask().getId())
                .title(subTask.getTitle())
                .description(subTask.getDescription())
                .status(subTask.getStatus())
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatar(assignee != null ? assignee.getAvatarUrl() : null)
                .estimatedHours(subTask.getEstimatedHours())
                .sortOrder(subTask.getSortOrder())
                .build();
    }
}
