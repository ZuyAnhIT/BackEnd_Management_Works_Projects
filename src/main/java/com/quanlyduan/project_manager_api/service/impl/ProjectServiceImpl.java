package com.quanlyduan.project_manager_api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.projection.ProjectView;
import com.quanlyduan.project_manager_api.security.SecurityServicePermission;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityServicePermission securityServicePermission;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request) {
        // 1) IDOR: workspace phai thuoc company trong URL
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: id=" + workspaceId));
        if (!Objects.equals(workspace.getCompany().getId(), companyId)) {
            throw new BadRequestException("Workspace does not belong to company: companyId=" + companyId);
        }

        // 2) Kiem tra trung projectCode (chi tinh project chua xoa mem)
        final String normalizedCode = request.getProjectCode().trim();
        final String normalizedName = request.getName().trim();

        if (projectRepository.countActiveProjectCode(workspaceId, normalizedCode) > 0L) {
            throw new BadRequestException("Project code already exists in this workspace: projectCode=" + normalizedCode);
        }

        // 3) Lay current user lam nguoi tao + validate managerId (neu co)
        Integer currentUserId = securityServicePermission.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Cannot identify current user");
        }
        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found: id=" + currentUserId));

        Integer managerId = request.getManagerId();
        if (managerId != null) {
            userRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager user not found: id=" + managerId));
        }

        // 4) projectTypeId: cho phep null; neu id khong ton tai thi coi nhu null
        Integer projectTypeId = request.getProjectTypeId();
        Integer effectiveProjectTypeId = projectTypeId;
        if (projectTypeId != null && projectRepository.countProjectTypeById(projectTypeId) == 0L) {
            effectiveProjectTypeId = null;
        }

        // 5) Ngay thang: dueDate phai >= startDate (neu ca hai cung duoc gui)
        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("dueDate must be on or after startDate");
        }

        // 6) Insert ban ghi (native) — ho tro JSON/nullable, dat default priority khi null
        try {
            projectRepository.insertProject(
                    workspaceId,
                    effectiveProjectTypeId,
                    normalizedName,
                    normalizedCode,
                    request.getDescription(),
                    request.getCoverImageUrl(),
                    request.getGoal(),
                    managerId,
                    (request.getPriority() != null ? request.getPriority().name() : null),
                    request.getStartDate(),
                    request.getDueDate(),
                    createdBy.getId(),
                    (request.getBoardConfig() != null ? request.getBoardConfig().toString() : null)
            );
        } catch (DataIntegrityViolationException ex) {
            String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            String msg = root != null ? root.toLowerCase() : "";
            if (msg.contains("duplicate entry") || msg.contains("uk_project_code") || (msg.contains("project_code") && msg.contains("workspace_id"))) {
                throw new BadRequestException("Project code already exists in this workspace: projectCode=" + normalizedCode);
            }
            if (msg.contains("foreign key constraint fails") && msg.contains("project_type_id")) {
                throw new BadRequestException("Invalid projectTypeId: not found or deleted");
            }
            if (msg.contains("foreign key constraint fails") && msg.contains("manager_id")) {
                throw new BadRequestException("Invalid managerId: not found or deleted");
            }
            throw new BadRequestException("Failed to create project: " + ex.getMostSpecificCause().getMessage());
        } catch (Exception ex) {
            throw new BadRequestException("Failed to create project: " + ex.getMessage());
        }

        // 7) Lay id vua tao + doc entity de lay status/timestamps tu DB
        Integer newProjectId = projectRepository.findIdByWorkspaceAndProjectCode(workspaceId, normalizedCode);
        if (newProjectId == null) {
            throw new BadRequestException("Cannot locate newly created project");
        }

        Project project = projectRepository.findById(newProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found after creation: id=" + newProjectId));

        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(workspace.getId())
                .name(normalizedName)
                .projectCode(normalizedCode)
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .goal(request.getGoal())
                .projectTypeId(effectiveProjectTypeId)
                .status(project.getStatus())
                .priority(request.getPriority() != null ? request.getPriority() : (project.getPriority() != null ? project.getPriority() : Priority.MEDIUM))
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .managerId(managerId)
                .createdById(createdBy.getId())
                .boardConfig(request.getBoardConfig())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    public List<ProjectResponse> listProjects(Integer companyId, Integer workspaceId) {
        // IDOR: workspace phai thuoc company; repo da loc deleted_at IS NULL
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: id=" + workspaceId));
        if (!Objects.equals(ws.getCompany().getId(), companyId)) {
            throw new BadRequestException("Workspace does not belong to company: companyId=" + companyId);
        }
        List<ProjectView> rows = projectRepository.findProjectsByWorkspace(companyId, workspaceId);
        return rows.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProject(Integer companyId, Integer workspaceId, Integer projectId) {
        // Repo loc theo companyId + workspaceId + projectId va chi tra project chua xoa mem
        ProjectView row = projectRepository.findProjectDetail(companyId, workspaceId, projectId);
        if (row == null) {
            throw new ResourceNotFoundException("Project not found: id=" + projectId);
        }
        return toResponse(row);
    }

    @Override
    @Transactional
    public ProjectResponse renameProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request) {
        // 1) Validate name
        String newName = request.getName() != null ? request.getName().trim() : null;
        if (newName == null || newName.isEmpty()) {
            throw new BadRequestException("Project name is required");
        }
        if (newName.length() > 255) {
            throw new BadRequestException("Project name must be at most 255 characters");
        }

        // 2) Kiem tra ton tai/IDOR qua detail
        ProjectView exist = projectRepository.findProjectDetail(companyId, workspaceId, projectId);
        if (exist == null) {
            throw new ResourceNotFoundException("Project not found: id=" + projectId);
        }

        // 3) Update ten + tra lai detail
        int updated = projectRepository.renameProject(companyId, workspaceId, projectId, newName);
        if (updated == 0) {
            throw new BadRequestException("Failed to rename project");
        }

        ProjectView after = projectRepository.findProjectDetail(companyId, workspaceId, projectId);
        return toResponse(after);
    }

    @Override
    @Transactional
    public void softDeleteProject(Integer companyId, Integer workspaceId, Integer projectId, String reason) {
        // 1) Kiem tra project ton tai (chua xoa)
        ProjectView row = projectRepository.findProjectDetail(companyId, workspaceId, projectId);
        if (row == null) {
            throw new ResourceNotFoundException("Project not found: id=" + projectId);
        }
        // 2) Lay current user lam deleted_by
        Integer currentUserId = securityServicePermission.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Cannot identify current user");
        }
        // 3) Set deleted_at/deleted_by/ly do
        int updated = projectRepository.softDeleteProject(companyId, workspaceId, projectId, currentUserId, reason);
        if (updated == 0) {
            throw new BadRequestException("Failed to soft delete project");
        }
    }

    private ProjectResponse toResponse(ProjectView v) {
        // Parse enum va JSON an toan (co the null hoac gia tri khong hop le)
        Priority prio = null;
        try {
            prio = v.getPriority() != null ? Priority.valueOf(v.getPriority()) : null;
        } catch (IllegalArgumentException ignored) {}

        ProjectStatus status = null;
        try {
            status = v.getStatus() != null ? ProjectStatus.valueOf(v.getStatus()) : null;
        } catch (IllegalArgumentException ignored) {}

        JsonNode board = null;
        if (v.getBoardConfig() != null) {
            try {
                board = objectMapper.readTree(v.getBoardConfig());
            } catch (JsonProcessingException ignored) {}
        }

        return ProjectResponse.builder()
                .id(v.getId())
                .workspaceId(v.getWorkspaceId())
                .projectTypeId(v.getProjectTypeId())
                .name(v.getName())
                .projectCode(v.getProjectCode())
                .description(v.getDescription())
                .coverImageUrl(v.getCoverImageUrl())
                .goal(v.getGoal())
                .status(status)
                .priority(prio)
                .startDate(v.getStartDate())
                .dueDate(v.getDueDate())
                .managerId(v.getManagerId())
                .createdById(v.getCreatedById())
                .boardConfig(board)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
