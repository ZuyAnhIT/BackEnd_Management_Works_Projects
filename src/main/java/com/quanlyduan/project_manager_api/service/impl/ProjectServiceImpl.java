package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectTypeRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Triển khai ProjectService cho US7 – Tạo Project mới.
 * - Tận dụng ánh xạ JPA sẵn có: gán quan hệ qua reference, không viết mapping thủ công phức tạp.
 * - Không sửa code cũ; chỉ thêm mới service để controller gọi.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ObjectMapper objectMapper;

    /**
     * US7: Tạo Project mới trong Workspace.
     * Logic & Nghiệp vụ:
     * 1) Xác thực Workspace tồn tại (404 nếu không thấy).
     * 2) Ràng buộc unique projectCode trong cùng workspace (400 nếu đã tồn tại).
     * 3) Lấy reference cho các quan hệ (workspace, createdBy, manager, projectType): không cần load đầy đủ.
     * 4) Thiết lập các trường scalar từ request; giữ mặc định entity cho status/priority/progress nếu request không cung cấp.
     * 5) Lưu Project, trả ProjectResponse (tránh trả Entity trực tiếp để an toàn serialize).
     */
    @Override
    @Transactional
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId) {
        // (1) Kiểm tra workspace tồn tại
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // (1b) Xác nhận workspace thuộc đúng companyId theo path (tránh truy cập chéo công ty)
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        // (2) Kiểm tra unique projectCode trong workspace (không phân biệt hoa thường)
        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
            throw new BadRequestException("Project code already exists in this workspace");
        }

        // (3) Lấy reference cho createdBy (không cần load entity đầy đủ)
        User createdBy = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // (4) Khởi tạo Project và gán các trường/quan hệ
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setCreatedBy(createdBy);

        // Scalar từ request
        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        project.setDescription(request.getDescription());
        project.setGoal(request.getGoal());
        project.setCoverImageUrl(request.getCoverImageUrl());
        // boardConfig: nhận JSON trực tiếp từ client, lưu dạng String JSON
        if (request.getBoardConfig() != null) {
            try {
                project.setBoardConfig(objectMapper.writeValueAsString(request.getBoardConfig()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid boardConfig JSON");
            }
        }
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());

        // Priority (nếu client gửi), nếu null giữ mặc định của entity
        // Nếu priority invalid hoặc null -> dùng MEDIUM (thân thiện hơn)
        if (request.getPriority() != null) {
            try {
                project.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                project.setPriority(Priority.MEDIUM);
            }
        }

        // Manager (optional)
        if (request.getManagerId() != null && request.getManagerId() > 0) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager user not found"));
            project.setManager(manager);
        }

        // Project Type (optional)
        if (request.getProjectTypeId() != null && request.getProjectTypeId() > 0) {
            ProjectType type = projectTypeRepository.findById(request.getProjectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project type not found"));
            project.setProjectType(type);
        }

        // Đảm bảo progress không null nếu entity không có default
        if (project.getProgress() == null) {
            project.setProgress(BigDecimal.ZERO);
        }

        // (5) Lưu và trả response
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    /**
     * Helper map Entity -> DTO (tối thiểu, không viết mapping phức tạp; chỉ rút gọn trường cần thiết).
     */
    private ProjectResponse toResponse(Project p) {
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
                .managerId(p.getManager() != null ? p.getManager().getId() : null)
                .managerName(p.getManager() != null ? p.getManager().getFullName() : null)
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null)
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
