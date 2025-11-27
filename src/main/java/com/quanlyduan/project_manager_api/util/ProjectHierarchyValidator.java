// File: src/main/java/com/quanlyduan/project_manager_api/util/ProjectHierarchyValidator.java
package com.quanlyduan.project_manager_api.util;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectHierarchyValidator {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    /**
     * Validate: Project -> Workspace -> Company
     */
    public Project validateProjectHierarchy(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Dự án không thuộc về Workspace này.");
        }
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace không thuộc về Công ty này.");
        }
        return project;
    }

    /**
     * Validate: Task -> Project -> Workspace -> Company
     * (Dùng cho các API thao tác trực tiếp trên Task như Assign, Comment)
     */
    public Task validateTaskHierarchy(Integer taskId) {
        // Lưu ý: Task Controller thường không nhận companyId/workspaceId trên URL (PUT /api/tasks/{id})
        // Nên ta chỉ cần tìm task tồn tại là được. Quyền hạn đã được @PreAuthorize lo.
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId));
    }
}

