package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProjectStatusServiceImpl implements com.quanlyduan.project_manager_api.service.ProjectStatusService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request) {
        // 1) Tìm project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // 2) Xác nhận quan hệ thuộc workspace và company từ path (không thay đổi code cũ)
        if (project.getWorkspace() == null || project.getWorkspace().getId() == null
                || !project.getWorkspace().getId().equals(workspaceId)
                || project.getWorkspace().getCompany() == null
                || project.getWorkspace().getCompany().getId() == null
                || !project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Project does not belong to the specified workspace/company");
        }

        // 3) Không cho chuyển sang CANCELLED qua API này (CANCELLED dành cho soft delete)
        ProjectStatus newStatus = request.getStatus();
        if (newStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Changing status to CANCELLED is not allowed via this API");
        }

        // 4) Cập nhật trạng thái và các trường liên quan
        project.setStatus(newStatus);
        if (newStatus == ProjectStatus.COMPLETED) {
            project.setCompletedAt(LocalDate.now());
        } else {
            project.setCompletedAt(null);
        }

        // 5) Lưu và trả về DTO
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    private ProjectResponse toResponse(Project p) {
        Integer managerId = (p.getManager() != null) ? p.getManager().getId() : null;
        String managerName = (p.getManager() != null) ? p.getManager().getFullName() : null;
        User createdBy = p.getCreatedBy();
        Integer createdById = (createdBy != null) ? createdBy.getId() : null;
        String createdByName = (createdBy != null) ? createdBy.getFullName() : null;

        return ProjectResponse.builder()
                .id(p.getId())
                .workspaceId(p.getWorkspace() != null ? p.getWorkspace().getId() : null)
                .name(p.getName())
                .projectCode(p.getProjectCode())
                .description(p.getDescription())
                .goal(p.getGoal())
                .coverImageUrl(p.getCoverImageUrl())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .priority(p.getPriority() != null ? p.getPriority().name() : null)
                .startDate(p.getStartDate())
                .dueDate(p.getDueDate())
                .completedAt(p.getCompletedAt())
                .progress(p.getProgress())
                .managerId(managerId)
                .managerName(managerName)
                .createdById(createdById)
                .createdByName(createdByName)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

