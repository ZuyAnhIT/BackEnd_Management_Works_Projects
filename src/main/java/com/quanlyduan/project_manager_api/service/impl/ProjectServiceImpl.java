package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectTypeRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;

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
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private boolean isProvided(String value) {
        return value != null && !value.isBlank() && !"string".equalsIgnoreCase(value.trim());
    }

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

            // (5) Lưu Project
        Project saved = projectRepository.save(project);

        // (6) Gán người tạo làm Project Admin
        Role projectAdminRole = roleRepository.findFirstByRoleCode(RoleCode.PROJECT_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + RoleCode.PROJECT_ADMIN.name() + ". Please configure the database."
                ));

        ProjectMember projectMember = ProjectMember.builder()
                .project(saved)
                .user(createdBy)
                .role(projectAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        projectMemberRepository.save(projectMember);

        // (7) Trả response
        return toResponse(saved);
    }
    

    /**
     * US8: Lấy danh sách Project trong Workspace.
     * Logic & Nghiệp vụ:
     * 1) Xác thực workspace tồn tại và thuộc companyId; sai → 400.
     * 2) Lấy danh sách project theo workspace.
     * 3) (Tuỳ chọn) Loại bỏ các project có status CANCELLED khỏi kết quả để tránh hiển thị dự án đã hủy.
     * 4) Map tối thiểu sang DTO ProjectResponse và trả về.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjectsByWorkspace(Integer companyId, Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        List<Project> projects = projectRepository.findByWorkspace_Id(workspaceId);

        // Yêu cầu hiển thị mới: hiển thị tất cả TRỪ CANCELLED
        List<Project> visible = projects.stream()
                .filter(p -> p.getStatus() == null || p.getStatus() != ProjectStatus.CANCELLED)
                .collect(Collectors.toList());

        return visible.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Project Trash: trả về các project có trạng thái CANCELLED trong workspace.
     * Logic:
     * 1) Xác thực workspace tồn tại và thuộc companyId (sai → 400).
     * 2) Lấy danh sách project theo workspace và lọc status = CANCELLED.
     * 3) Map sang DTO và trả về.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> listCancelledProjectsByWorkspace(Integer companyId, Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        List<Project> projects = projectRepository.findByWorkspace_Id(workspaceId);
        List<Project> trashed = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.CANCELLED)
                .collect(Collectors.toList());

        return trashed.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * US9: Xóa dự án (soft delete) bằng cách chuyển trạng thái sang CANCELLED.
     * Logic & Nghiệp vụ:
     * 1) Xác thực workspace tồn tại và thuộc companyId (sai → 400) để ngăn truy cập chéo công ty.
     * 2) Lấy Project theo projectId (404 nếu không tồn tại).
     * 3) Kiểm tra Project thuộc đúng workspaceId trong path (sai → 400).
     * 4) Set status = CANCELLED và lưu. Không xóa cứng để giữ dữ liệu lịch sử/liên kết.
     */
    @Override
    @Transactional
    public void deleteProject(Integer companyId, Integer workspaceId, Integer projectId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace");
        }

        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace");
        }

        ProjectStatus newStatus = request.getNewStatus();
        if (newStatus == null) {
            throw new BadRequestException("New status must not be null");
        }
        if (newStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot update status to CANCELLED. Use the delete endpoint instead.");
        }

        if (project.getStatus() == newStatus) {
            throw new BadRequestException("Project is already in the requested status.");
        }

        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace");
        }

        // name
        if (isProvided(request.getName())) {
            project.setName(request.getName());
        }

        // projectCode: ensure unique within workspace if changed
        if (isProvided(request.getProjectCode())) {
            String newCode = request.getProjectCode();
            String currentCode = project.getProjectCode();
            if (!newCode.equalsIgnoreCase(currentCode)) {
                if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, newCode)) {
                    throw new BadRequestException("Project code already exists in this workspace");
                }
                project.setProjectCode(newCode);
            }
        }

        if (isProvided(request.getDescription())) {
            project.setDescription(request.getDescription());
        }
        if (isProvided(request.getGoal())) {
            project.setGoal(request.getGoal());
        }
        if (isProvided(request.getCoverImageUrl())) {
            project.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }
        if (request.getCompletedAt() != null) {
            project.setCompletedAt(request.getCompletedAt());
        }

        // managerId: null -> giữ nguyên; 0 -> giữ nguyên; >0 -> cập nhật
        if (request.getManagerId() != null) {
            Integer managerId = request.getManagerId();
            if (managerId != 0) {
                User manager = userRepository.findById(managerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
                project.setManager(manager);
            }
        }

        // projectTypeId
        if (request.getProjectTypeId() != null) {
            Integer projectTypeId = request.getProjectTypeId();
            if (projectTypeId == 0) {
                project.setProjectType(null);
            } else {
                ProjectType type = projectTypeRepository.findById(projectTypeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Project type not found"));
                project.setProjectType(type);
            }
        }

        if (isProvided(request.getBoardConfig())) {
            // Optional: validate JSON format only when provided and not placeholder
            try {
                objectMapper.readTree(request.getBoardConfig());
            } catch (Exception e) {
                throw new BadRequestException("boardConfig is not valid JSON");
            }
            project.setBoardConfig(request.getBoardConfig());
        }

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
    @Override
    public ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Kiểm tra project có thuộc workspace và company tương ứng không
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace");
        }

        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company");
        }

        // Dùng mapper chung để đảm bảo đầy đủ field như khi tạo/list
        return toResponse(project);
    }
}
