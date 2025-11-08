package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.security.SecurityServicePermission;
import com.quanlyduan.project_manager_api.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

/**
 * Service US 7: Create Project
 * Flow: (1) load workspace → (2) verify workspace ∈ company (IDOR) →
 * (3) check duplicate projectCode (workspace-scoped, case-insensitive) →
 * (4) resolve current user (creator) → (5) validate managerId (optional) →
 * (6) insert record → (7) fetch new id → (8) build response.
 * Authorization enforced at controller via @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityServicePermission securityServicePermission;

    @Override
    @Transactional
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request) {
        // 1) Workspace tồn tại
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: id=" + workspaceId));

        // 2) IDOR: workspace phải thuộc companyId trên URL
        if (!Objects.equals(workspace.getCompany().getId(), companyId)) {
            throw new BadRequestException("Workspace does not belong to company: companyId=" + companyId);
        }

        // 3) Trùng mã (workspace-scoped, case-insensitive)
        // Chuẩn hoá dữ liệu để so sánh/ghi ổn định
        final String normalizedCode = request.getProjectCode().trim();
        final String normalizedName = request.getName().trim();

        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, normalizedCode)) {
            throw new BadRequestException("Project code already exists in this workspace: projectCode=" + normalizedCode);
        }

        // 4) Người tạo hiện tại
        Integer currentUserId = securityServicePermission.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Cannot identify current user");
        }
        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found: id=" + currentUserId));

        // 5) managerId (tùy chọn)
        Integer managerId = request.getManagerId();
        if (managerId != null) {
            userRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager user not found: id=" + managerId));
        }

        // 5.2) projectTypeId (tùy chọn): validate tồn tại để tránh lỗi FK mơ hồ
        Integer projectTypeId = request.getProjectTypeId();
        // Cho phép null: nếu gửi id nhưng không tồn tại, coi như null (không ràng buộc FK)
        Integer effectiveProjectTypeId = projectTypeId;
        if (projectTypeId != null && projectRepository.countProjectTypeById(projectTypeId) == 0L) {
            effectiveProjectTypeId = null;
        }

        // 5.1) Ràng buộc ngày tháng (nếu truyền đủ)
        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("dueDate must be on or after startDate");
        }

        // 6) Tạo bản ghi
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
            // Phân loại một số lỗi phổ biến cho thông điệp rõ ràng
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
            // Thông điệp ngắn gọn, dễ hiểu cho client/dev
            throw new BadRequestException("Failed to create project: " + ex.getMessage());
        }

        // 7) Lấy ID mới theo (workspaceId, projectCode)
        Integer newProjectId = projectRepository.findIdByWorkspaceAndProjectCode(workspaceId, normalizedCode);
        if (newProjectId == null) {
            throw new BadRequestException("Cannot locate newly created project");
        }

        // 8) Đọc entity để lấy id/status/timestamps
        Project project = projectRepository.findById(newProjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found after creation: id=" + newProjectId));

        // 9) Mapping ProjectResponse
        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(workspace.getId())
                .name(normalizedName)
                .projectCode(normalizedCode)
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .goal(request.getGoal())
                .projectTypeId(request.getProjectTypeId())
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
}
